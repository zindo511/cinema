package vn.cinema.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cinema.app.dto.response.BookingResponse;
import vn.cinema.app.dto.response.BookingSeatResponse;
import vn.cinema.app.mapper.BookingMapper;
import vn.cinema.domain.booking.entity.Booking;
import vn.cinema.domain.booking.entity.BookingSeat;
import vn.cinema.domain.booking.entity.BookingStatus;
import vn.cinema.domain.booking.repository.BookingRepository;
import vn.cinema.domain.booking.repository.BookingSeatRepository;
import vn.cinema.domain.cinema.entity.Seat;
import vn.cinema.domain.common.exception.BusinessErrorCode;
import vn.cinema.domain.common.exception.ConflictException;
import vn.cinema.domain.common.exception.ResourceNotFoundException;
import vn.cinema.domain.showtime.entity.Showtime;
import vn.cinema.domain.showtime.entity.ShowtimeSeat;
import vn.cinema.domain.showtime.entity.ShowtimeSeatStatus;
import vn.cinema.domain.showtime.repository.ShowtimeRepository;
import vn.cinema.domain.showtime.repository.ShowtimeSeatRepository;
import vn.cinema.infrastructure.utility.BookingCodeGenerator;

import java.awt.print.Book;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeSeatRepository showtimeSeatRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final Clock clock;
    private static final Duration MIN_BOOKING_LEAD_TIME = Duration.ofMinutes(30);
    private final BookingCodeGenerator bookingCodeGenerator;
    private final BookingMapper bookingMapper;

    @Transactional
    public BookingResponse createBooking(
            Long customerId,
            Long showtimeId,
            List<Long> seatIds,
            UUID idempotencyKey
    ) {
        // Sinh hash đại diện cho nội dung request (showtimeId + seatIds)
        String requestHash = generateRequestHash(showtimeId, seatIds);

        // Check Idempotency
        Optional<Booking> existingBookingOpt = bookingRepository.findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey);

        if (existingBookingOpt.isPresent()) {
            Booking existingBooking = existingBookingOpt.get();

            // nếu dùng lại key nhưng payload bị thay đổi --> conflict
            if (!existingBooking.getRequestHash().equals(requestHash)) {
                throw new ConflictException(
                        BusinessErrorCode.IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD,
                        "Idempotency key was reused with a different request payload"
                );
            }
            BookingResponse bookingResponse = bookingMapper.toBookingResponse(existingBooking);
            bookingResponse.setSeats(seatResponses(existingBooking));
            return bookingResponse;
        }

        // REQUEST MỚI
        Instant now = clock.instant();

        // validate showtime
        Showtime showtime = validateShowtimeForBooking(showtimeId, now);

        // lock và validate ghế theo thứ tự ID tăng dần.
        List<ShowtimeSeat> lockedSeats = lockSelectedShowtimeSeats(showtimeId, seatIds);

        // Tổng tiền tính từ giá snapshot trong showtime_seat
        BigDecimal totalAmount = lockedSeats.stream()
                .map(ShowtimeSeat::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tạo booking
        Booking booking = Booking.builder()
                .bookingCode(bookingCodeGenerator.generateWithHyphen())
                .customerId(customerId)
                .showtime(showtime)
                .status(BookingStatus.PENDING)
                .totalAmount(totalAmount)
                .expiresAt(now.plus(Duration.ofMinutes(10)))
                .idempotencyKey(idempotencyKey)
                .requestHash(requestHash)
                .build();

        bookingRepository.save(booking);

        // tạo snapshot BookingSeat
        List<BookingSeat> bookingSeats = lockedSeats.stream()
                .map(showtimeSeat -> {
                    Seat physicalSeat = showtimeSeat.getSeat();

                    return BookingSeat.builder()
                            .booking(booking)
                            .showtimeSeat(showtimeSeat)
                            .seatLabel(
                                    physicalSeat.getSeatRow() + physicalSeat.getSeatNumber()
                            )
                            .seatType(physicalSeat.getSeatType())
                            .price(showtimeSeat.getPrice())
                            .build();
                })
                .toList();
        bookingSeatRepository.saveAll(bookingSeats);

        // Đánh dấu ghế đang được giữ. AVAILABLE --> HELD
        lockedSeats.forEach(seat -> seat.hold(now));

        // chuyển sang BookingResponse
        BookingResponse bookingResponse = bookingMapper.toBookingResponse(booking);
        bookingResponse.setSeats(seatResponses(booking));
        return bookingResponse;
    }

    // PRIVATE HELPER
    private Showtime validateShowtimeForBooking(Long showtimeId, Instant now) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        BusinessErrorCode.RESOURCE_NOT_FOUND,
                        "Showtime not found with ID: " + showtimeId
                ));

        showtime.ensureBookable(clock.instant());

        Duration timeUntilStart = Duration.between(now, showtime.getStartTime());

        if (timeUntilStart.compareTo(MIN_BOOKING_LEAD_TIME) < 0) {
            throw new ConflictException(
                    BusinessErrorCode.SHOWTIME_NOT_BOOKABLE,
                    "Online booking closes 30 minutes before the showtime starts"
            );
        }

        return showtime;
    }

    @Transactional
    public List<ShowtimeSeat> lockSelectedShowtimeSeats(Long showtimeId, List<Long> seatIds) {
        // Check showtimeSeat
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("Seat IDs cannot be null or empty");
        }

        if (seatIds.size() > 8) {
            throw new IllegalArgumentException("A booking cannot contain more than 8 seats");
        }

        // Sắp xếp danh sách seatIds để chống Deadlock
        List<Long> sortedSeatIds = seatIds.stream().distinct().sorted().toList();

        List<ShowtimeSeat> lockedSeats = showtimeSeatRepository.findAllByIdInForUpdate(
                showtimeId, sortedSeatIds
        );

        // Có ghế không tồn tại hoặc không thuộc showtime được yêu cầu.
        if (lockedSeats.size() != sortedSeatIds.size()) {
            throw new ResourceNotFoundException(
                    "One or more seats do not exist or do not belong to showtime ID: "
                            + showtimeId
            );
        }

        for (ShowtimeSeat seat : lockedSeats) {
            if (seat.getStatus() != ShowtimeSeatStatus.AVAILABLE) {
                throw new ConflictException(
                        BusinessErrorCode.SEAT_NOT_AVAILABLE,
                        "Seat with ID " + seat.getId()
                                + " is not available. Current status: "
                                + seat.getStatus()
                );
            }
        }
        return lockedSeats;
    }

    // hàm hash request
    private String generateRequestHash(Long showtimeId, List<Long> seatIds) {
        List<Long> sortedSeatIds = seatIds.stream().distinct().sorted().toList();

        String rawPayload = showtimeId + ":" + sortedSeatIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        // Hash SHA-256
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawPayload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes); // Trả về chuỗi Hex 64 ký tự
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // hàm lấy các seat cho Booking
    private List<BookingSeatResponse> seatResponses(Booking booking) {
        return booking.getBookingSeats().stream()
                .map(seat -> new BookingSeatResponse(
                        seat.getShowtimeSeat().getId(),
                        seat.getSeatLabel(),
                        seat.getSeatType(),
                        seat.getPrice()
                ))
                .toList();
    }
}
