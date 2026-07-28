package vn.cinema.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.cinema.app.dto.request.BookingRequest;
import vn.cinema.app.dto.request.PaymentRequest;
import vn.cinema.app.dto.response.BookingResponse;
import vn.cinema.app.dto.response.CreatePaymentResponse;
import vn.cinema.app.service.BookingService;
import vn.cinema.app.service.PaymentService;
import vn.cinema.infrastructure.utility.IpUtils;

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
}
