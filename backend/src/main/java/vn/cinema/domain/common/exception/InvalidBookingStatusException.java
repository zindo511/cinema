package vn.cinema.domain.common.exception;

import vn.cinema.domain.booking.entity.BookingStatus;

public class InvalidBookingStatusException extends BusinessRuleException {

    public InvalidBookingStatusException(String transition, BookingStatus currentStatus) {
        super(
                BusinessErrorCode.INVALID_BOOKING_STATUS,
                "Cannot " + transition + " booking in status " + currentStatus
        );
    }
}
