package vn.cinema.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.cinema.app.dto.request.BookingRequest;
import vn.cinema.app.dto.request.PaymentRequest;
import vn.cinema.app.dto.response.BookingHistoryResponse;
import vn.cinema.app.dto.response.BookingResponse;
import vn.cinema.app.dto.response.CreatePaymentResponse;
import vn.cinema.app.service.BookingService;
import vn.cinema.app.service.PaymentService;
import vn.cinema.domain.booking.entity.BookingStatus;
import vn.cinema.infrastructure.utility.IpUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestHeader("X-Idempotency-Key") UUID idempotencyKey,
            @Valid @RequestBody BookingRequest bookingRequest,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long customerId = ((Number) Objects.requireNonNull(jwt.getClaim("userId"))).longValue();

        BookingResponse response = bookingService.createBooking(
                customerId,
                bookingRequest.getShowtimeId(),
                bookingRequest.getSeatIds(),
                idempotencyKey
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/payment")
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @PathVariable Long bookingId,
            @Valid @RequestBody PaymentRequest paymentRequest,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        Long customerId = ((Number) Objects.requireNonNull(
                jwt.getClaim("userId")
        )).longValue();

        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createVnPayPayment(
                customerId, bookingId, paymentRequest, IpUtils.getClientIp(request))
        );
    }

    @GetMapping("/history")
    public ResponseEntity<Page<BookingHistoryResponse>> getBookingHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) BookingStatus status
    ) {
        Long customerId = ((Number) Objects.requireNonNull(
                jwt.getClaim("userId")
        )).longValue();

        return ResponseEntity.status(HttpStatus.OK).body(bookingService.getBookingHistory(customerId, start, end, page, size, status));
    }

    @PostMapping("/{bookingCode}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable String bookingCode,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = ((Number) Objects.requireNonNull(
                jwt.getClaim("userId")
        )).longValue();
        bookingService.cancelBooking(bookingCode, userId);
        return ResponseEntity.noContent().build();
    }
}
