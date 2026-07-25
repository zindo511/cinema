package vn.cinema.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.cinema.app.dto.request.BookingRequest;
import vn.cinema.app.dto.response.BookingResponse;
import vn.cinema.app.service.BookingService;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

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
}
