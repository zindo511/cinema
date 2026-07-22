package vn.cinema.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import vn.cinema.app.dto.request.CreateBookingRequest;
import vn.cinema.app.dto.response.BookingCreationResult;
import vn.cinema.app.mapper.BookingMapper;
import vn.cinema.domain.booking.entity.Booking;
import vn.cinema.domain.booking.entity.BookingSeat;
import vn.cinema.domain.booking.repository.BookingRepository;
import vn.cinema.domain.booking.repository.BookingSeatRepository;
import vn.cinema.domain.common.exception.BusinessErrorCode;
import vn.cinema.domain.common.exception.BusinessRuleException;
import vn.cinema.domain.common.exception.ConflictException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final BookingTransactionService bookingTransactionService;
    private final BookingMapper bookingMapper;

    public BookingCreationResult createBooking(
            Long customerId,
            UUID idempotencyKey,
            CreateBookingRequest request
    ) {
        validateIdentityAndRequest(customerId, idempotencyKey, request);

        List<Long> sortedSeatIds = normalizeSeatIds(request.getShowtimeSeatIds());
        String requestHash = hashRequest(request.getShowtimeId(), sortedSeatIds);

        var existing = bookingRepository.findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey);
        if (existing.isPresent()) {
            return replayOrConflict(existing.get(), requestHash);
        }

        try {
            return bookingTransactionService.createNewBooking(
                    customerId,
                    idempotencyKey,
                    requestHash,
                    request.getShowtimeId(),
                    sortedSeatIds
            );
        } catch (DataIntegrityViolationException exception) {
            // The failed write transaction has rolled back; safely resolve a concurrent idempotent insert now.
            return bookingRepository.findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey)
                    .map(booking -> replayOrConflict(booking, requestHash))
                    .orElseThrow(() -> new ConflictException(
                            BusinessErrorCode.BOOKING_CREATION_CONFLICT,
                            "Booking could not be created due to a concurrent request; please retry"
                    ));
        }
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

    private void validateIdentityAndRequest(
            Long customerId,
            UUID idempotencyKey,
            CreateBookingRequest request
    ) {
        if (customerId == null || customerId <= 0) {
            throw invalidRequest("Customer ID must be a positive number");
        }
        if (idempotencyKey == null) {
            throw invalidRequest("Idempotency-Key header must be a valid UUID");
        }
        if (request == null || request.getShowtimeId() == null || request.getShowtimeId() <= 0) {
            throw invalidRequest("Showtime ID must be a positive number");
        }
    }

    private List<Long> normalizeSeatIds(List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty() || seatIds.size() > 8) {
            throw invalidRequest("A booking must contain between 1 and 8 seats");
        }
        if (seatIds.stream().anyMatch(seatId -> seatId == null || seatId <= 0)) {
            throw invalidRequest("Showtime seat IDs must be positive numbers");
        }
        if (new HashSet<>(seatIds).size() != seatIds.size()) {
            throw invalidRequest("Showtime seat IDs must not contain duplicates");
        }
        return seatIds.stream().sorted().toList();
    }

    private String hashRequest(Long showtimeId, List<Long> sortedSeatIds) {
        String canonicalRequest = showtimeId + ":" + sortedSeatIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonicalRequest.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private BusinessRuleException invalidRequest(String message) {
        return new BusinessRuleException(BusinessErrorCode.INVALID_REQUEST, message);
    }
}
