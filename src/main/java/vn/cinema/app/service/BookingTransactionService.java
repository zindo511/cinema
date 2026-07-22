package vn.cinema.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cinema.app.dto.response.BookingCreationResult;
import vn.cinema.app.mapper.BookingMapper;
import vn.cinema.config.BookingCodeGenerator;
import vn.cinema.config.BookingProperties;
import vn.cinema.domain.booking.entity.Booking;
import vn.cinema.domain.booking.entity.BookingSeat;
import vn.cinema.domain.booking.entity.BookingStatus;
import vn.cinema.domain.booking.repository.BookingRepository;
import vn.cinema.domain.booking.repository.BookingSeatRepository;
import vn.cinema.domain.common.exception.BusinessErrorCode;
import vn.cinema.domain.common.exception.ConflictException;
import vn.cinema.domain.common.exception.ResourceNotFoundException;
import vn.cinema.domain.showtime.entity.Showtime;
import vn.cinema.domain.showtime.entity.ShowtimeSeat;
import vn.cinema.domain.showtime.entity.ShowtimeSeatStatus;
import vn.cinema.domain.showtime.entity.ShowtimeStatus;
import vn.cinema.domain.showtime.repository.ShowtimeRepository;
import vn.cinema.domain.showtime.repository.ShowtimeSeatRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingTransactionService {

    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeSeatRepository showtimeSeatRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final BookingCodeGenerator bookingCodeGenerator;
    private final BookingProperties bookingProperties;
    private final BookingMapper bookingMapper;
    private final Clock clock;

    @Transactional
    public BookingCreationResult createNewBooking(
            Long customerId,
            UUID idempotencyKey,
            String requestHash,
            Long showtimeId,
            List<Long> sortedSeatIds
    ) {
        // Always lock the showtime before locking its seats.
        Showtime showtime = showtimeRepository.findByIdForUpdate(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Showtime not found with ID: " + showtimeId));

        // Recheck after acquiring the showtime lock in case a concurrent request committed first.
        var existing = bookingRepository.findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey);
        if (existing.isPresent()) {
            return replayOrConflict(existing.get(), requestHash);
        }

        Instant now = clock.instant();
        validateShowtime(showtime, now);

        List<ShowtimeSeat> lockedSeats = showtimeSeatRepository.findAllByIdForUpdate(
                showtimeId,
                sortedSeatIds
        );
        validateSeats(showtimeId, sortedSeatIds, lockedSeats);

        BigDecimal totalAmount = lockedSeats.stream()
                .map(ShowtimeSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Booking booking = Booking.builder()
                .bookingCode(bookingCodeGenerator.generate())
                .customerId(customerId)
                .showtime(showtime)
                .status(BookingStatus.PENDING)
                .totalAmount(totalAmount)
                .expiresAt(now.plus(bookingProperties.holdDuration()))
                .idempotencyKey(idempotencyKey)
                .requestHash(requestHash)
                .build();

        // Flush here so unique booking-code/idempotency violations surface inside this transaction.
        booking = bookingRepository.saveAndFlush(booking);

        Booking savedBooking = booking;
        List<BookingSeat> bookingSeats = lockedSeats.stream()
                .map(showtimeSeat -> toBookingSeat(savedBooking, showtimeSeat))
                .toList();

        lockedSeats.forEach(showtimeSeat -> showtimeSeat.hold(now));
        bookingSeatRepository.saveAll(bookingSeats);
        bookingSeatRepository.flush();

        return new BookingCreationResult(bookingMapper.toResponse(booking, bookingSeats), true);
    }

    private BookingCreationResult replayOrConflict(Booking existing, String requestHash) {
        if (!existing.getRequestHash().equals(requestHash)) {
            throw new ConflictException(
                    BusinessErrorCode.IDEMPOTENCY_KEY_REUSED,
                    "Idempotency key has already been used with a different booking request"
            );
        }

        List<BookingSeat> bookingSeats = bookingSeatRepository.findAllByBookingIdOrderById(existing.getId());
        return new BookingCreationResult(bookingMapper.toResponse(existing, bookingSeats), false);
    }

    private void validateShowtime(Showtime showtime, Instant now) {
        if (showtime.getStatus() != ShowtimeStatus.OPEN || !showtime.getStartTime().isAfter(now)) {
            throw new ConflictException(
                    BusinessErrorCode.SHOWTIME_NOT_BOOKABLE,
                    "Showtime is not open for booking or has already started"
            );
        }
    }

    private void validateSeats(
            Long showtimeId,
            List<Long> requestedSeatIds,
            List<ShowtimeSeat> lockedSeats
    ) {
        if (lockedSeats.size() != requestedSeatIds.size()) {
            throw new ResourceNotFoundException(
                    "One or more showtime seats do not exist or do not belong to showtime " + showtimeId
            );
        }

        boolean unavailable = lockedSeats.stream().anyMatch(showtimeSeat ->
                showtimeSeat.getStatus() != ShowtimeSeatStatus.AVAILABLE
                        || !Boolean.TRUE.equals(showtimeSeat.getSeat().getIsActive()));
        if (unavailable) {
            throw new ConflictException(
                    BusinessErrorCode.SEAT_NOT_AVAILABLE,
                    "One or more selected seats are no longer available"
            );
        }
    }

    private BookingSeat toBookingSeat(Booking booking, ShowtimeSeat showtimeSeat) {
        var seat = showtimeSeat.getSeat();
        return BookingSeat.builder()
                .booking(booking)
                .showtimeSeat(showtimeSeat)
                .seatLabel(seat.getSeatRow() + seat.getSeatNumber())
                .seatType(seat.getSeatType())
                .price(showtimeSeat.getPrice())
                .build();
    }
}
