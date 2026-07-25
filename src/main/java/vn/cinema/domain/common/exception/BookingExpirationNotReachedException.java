package vn.cinema.domain.common.exception;

import java.time.Instant;

public class BookingExpirationNotReachedException extends BusinessRuleException {

    public BookingExpirationNotReachedException(Instant expiresAt) {
        super(
                BusinessErrorCode.BOOKING_EXPIRATION_NOT_REACHED,
                "Cannot expire booking before " + expiresAt
        );
    }
}
