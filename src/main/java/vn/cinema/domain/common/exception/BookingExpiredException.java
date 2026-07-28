package vn.cinema.domain.common.exception;

import java.time.Instant;

public class BookingExpiredException extends BusinessRuleException {

    public BookingExpiredException(Instant expiresAt) {
        super(
                BusinessErrorCode.BOOKING_EXPIRED,
                "Booking is expired with expireAt: " + expiresAt
        );
    }
}
