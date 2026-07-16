package vn.cinema.domain.booking.entity;

import org.junit.jupiter.api.Test;
import vn.cinema.domain.common.exception.BusinessErrorCode;
import vn.cinema.domain.common.exception.BusinessRuleException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingTests {

    @Test
    void expiresPendingBooking() {
        Booking booking = Booking.builder().status(BookingStatus.PENDING).build();

        booking.expire();

        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
    }

    @Test
    void rejectsInvalidStatusTransitionWithStableCode() {
        Booking booking = Booking.builder().status(BookingStatus.CONFIRMED).build();

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, booking::expire);

        assertEquals(BusinessErrorCode.INVALID_BOOKING_STATUS.name(), exception.getCode());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }
}
