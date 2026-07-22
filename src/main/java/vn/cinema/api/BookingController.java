package vn.cinema.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.cinema.app.dto.request.CreateBookingRequest;
import vn.cinema.app.dto.response.BookingCreationResult;
import vn.cinema.app.dto.response.BookingResponse;
import vn.cinema.app.service.BookingService;
import vn.cinema.domain.booking.port.CurrentCustomerPort;
import vn.cinema.domain.common.exception.BusinessErrorCode;
import vn.cinema.domain.common.exception.BusinessRuleException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final CurrentCustomerPort currentCustomerPort;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        BookingCreationResult result = bookingService.createBooking(
                currentCustomerPort.getCurrentCustomerId(),
                parseIdempotencyKey(idempotencyKey),
                request
        );

        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.response());
    }

    private UUID parseIdempotencyKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            throw new BusinessRuleException(
                    BusinessErrorCode.INVALID_REQUEST,
                    "Idempotency-Key header is required"
            );
        }
        try {
            return UUID.fromString(rawKey.trim());
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException(
                    BusinessErrorCode.INVALID_REQUEST,
                    "Idempotency-Key header must be a valid UUID"
            );
        }
    }
}
