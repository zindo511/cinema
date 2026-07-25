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
import vn.cinema.domain.common.exception.BusinessRuleException;
import vn.cinema.domain.common.exception.ResourceNotFoundException;
import vn.cinema.domain.showtime.entity.Showtime;
import vn.cinema.domain.showtime.entity.ShowtimeSeat;
import vn.cinema.domain.showtime.entity.ShowtimeSeatStatus;
import vn.cinema.domain.showtime.repository.ShowtimeRepository;
import vn.cinema.domain.showtime.repository.ShowtimeSeatRepository;
import vn.cinema.infrastructure.utility.BookingCodeGenerator;

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

    /**
     * Tạo một đơn đặt vé mới cho khách hàng.
     *
     * <p>Quy trình xử lý:</p>
     * <ol>
     *   <li>Kiểm tra idempotency: nếu đã tồn tại booking với cùng idempotencyKey,
     *       trả về kết quả cũ (tránh tạo trùng khi client retry). Nếu key trùng nhưng
     *       payload khác thì ném {@link ConflictException}.</li>
     *   <li>Validate suất chiếu: đảm bảo suất chiếu tồn tại, còn cho phép đặt vé,
     *       và chưa quá hạn đặt trước (tối thiểu 30 phút trước giờ chiếu).</li>
     *   <li>Khoá (lock) các ghế được chọn theo thứ tự ID tăng dần để tránh deadlock,
     *       đồng thời kiểm tra trạng thái ghế phải là AVAILABLE.</li>
     *   <li>Tính tổng tiền từ giá snapshot của từng ghế trong suất chiếu.</li>
     *   <li>Tạo bản ghi Booking với trạng thái PENDING, thời hạn hết hạn 10 phút.</li>
     *   <li>Tạo các bản ghi BookingSeat (snapshot thông tin ghế tại thời điểm đặt).</li>
     *   <li>Chuyển trạng thái ghế từ AVAILABLE sang HELD.</li>
     * </ol>
     *
     * @param customerId     ID của khách hàng thực hiện đặt vé
     * @param showtimeId     ID của suất chiếu muốn đặt
     * @param seatIds        danh sách ID các ghế muốn đặt (tối đa 8 ghế)
     * @param idempotencyKey khoá idempotency do client gửi lên để tránh đặt trùng khi retry
     * @return {@link BookingResponse} chứa thông tin booking và danh sách ghế đã đặt
     * @throws ConflictException           nếu idempotencyKey bị tái sử dụng với payload khác,
     *                                     hoặc ghế không còn trạng thái AVAILABLE
     * @throws ResourceNotFoundException   nếu suất chiếu hoặc ghế không tồn tại
     * @throws BusinessRuleException       nếu danh sách ghế rỗng hoặc vượt quá 8 ghế (INVALID_REQUEST)
     */
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

    /**
     * Kiểm tra tính hợp lệ của suất chiếu trước khi cho phép đặt vé.
     *
     * <p>Thực hiện các bước kiểm tra:</p>
     * <ul>
     *   <li>Suất chiếu phải tồn tại trong hệ thống.</li>
     *   <li>Suất chiếu phải ở trạng thái cho phép đặt vé (gọi {@code ensureBookable}).</li>
     *   <li>Thời gian hiện tại phải cách giờ chiếu ít nhất 30 phút
     *       ({@link #MIN_BOOKING_LEAD_TIME}) – ngắt đặt vé online trước giờ chiếu.</li>
     * </ul>
     *
     * @param showtimeId ID của suất chiếu cần kiểm tra
     * @param now        thời điểm hiện tại (lấy từ {@link Clock})
     * @return đối tượng {@link Showtime} hợp lệ, sẵn sàng để đặt vé
     * @throws ResourceNotFoundException nếu không tìm thấy suất chiếu
     * @throws ConflictException         nếu suất chiếu không thể đặt hoặc đã quá hạn đặt trước
     */
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

    /**
     * Khoá (pessimistic lock) các ghế được chọn trong suất chiếu để đảm bảo
     * không có giao dịch khác chiếm ghế cùng lúc.
     *
     * <p>Chi tiết xử lý:</p>
     * <ul>
     *   <li>Validate: danh sách ghế không được rỗng và tối đa 8 ghế.</li>
     *   <li>Sắp xếp seatIds tăng dần và loại bỏ trùng lặp để tránh deadlock
     *       khi nhiều giao dịch cùng khoá các ghế.</li>
     *   <li>Sử dụng {@code SELECT ... FOR UPDATE} (qua repository) để khoá các bản ghi ghế.</li>
     *   <li>Kiểm tra số lượng ghế trả về khớp với yêu cầu (phát hiện ghế không tồn tại
     *       hoặc không thuộc suất chiếu).</li>
     *   <li>Kiểm tra tất cả ghế phải ở trạng thái {@code AVAILABLE}.</li>
     * </ul>
     *
     * @param showtimeId ID của suất chiếu chứa các ghế
     * @param seatIds    danh sách ID ghế cần khoá (tối đa 8, không được rỗng)
     * @return danh sách {@link ShowtimeSeat} đã được khoá, sẵn sàng để giữ chỗ
     * @throws BusinessRuleException       nếu danh sách ghế rỗng hoặc vượt quá 8 (INVALID_REQUEST)
     * @throws ConflictException           nếu có ghế không ở trạng thái AVAILABLE
     * @throws ResourceNotFoundException   nếu có ghế không tồn tại hoặc không thuộc suất chiếu
     */
    @Transactional
    public List<ShowtimeSeat> lockSelectedShowtimeSeats(Long showtimeId, List<Long> seatIds) {
        // Check showtimeSeat
        if (seatIds == null || seatIds.isEmpty()) {
            throw new BusinessRuleException(BusinessErrorCode.INVALID_REQUEST, "Seat IDs cannot be null or empty");
        }

        if (seatIds.size() > 8) {
            throw new BusinessRuleException(BusinessErrorCode.INVALID_REQUEST, "A booking cannot contain more than 8 seats");
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

    /**
     * Sinh chuỗi hash SHA-256 đại diện cho nội dung request đặt vé.
     *
     * <p>Hash được tạo từ chuỗi {@code "showtimeId:seatId1,seatId2,..."} (seatIds đã được
     * sắp xếp tăng dần và loại bỏ trùng lặp). Dùng để so sánh nhanh khi kiểm tra
     * idempotency – nếu client gửi lại cùng idempotencyKey nhưng payload khác (khác
     * showtime hoặc danh sách ghế), hash sẽ khác nhau → phát hiện xung đột.</p>
     *
     * @param showtimeId ID suất chiếu
     * @param seatIds    danh sách ID ghế
     * @return chuỗi hex 64 ký tự (SHA-256) đại diện cho nội dung request
     * @throws RuntimeException nếu thuật toán SHA-256 không khả dụng (trường hợp hiếm gặp)
     */
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

    /**
     * Chuyển đổi danh sách {@link BookingSeat} của một booking thành danh sách
     * {@link BookingSeatResponse} để trả về cho client.
     *
     * <p>Mỗi BookingSeatResponse chứa: ID ghế trong suất chiếu, nhãn ghế (ví dụ "A5"),
     * loại ghế (VIP, STANDARD,...) và giá vé tại thời điểm đặt.</p>
     *
     * @param booking đối tượng Booking cần lấy thông tin ghế
     * @return danh sách {@link BookingSeatResponse} chứa thông tin các ghế đã đặt
     */
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
