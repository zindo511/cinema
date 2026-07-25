package vn.cinema.domain.booking.entity;

import org.junit.jupiter.api.Test;
import vn.cinema.domain.common.exception.BookingExpirationNotReachedException;
import vn.cinema.domain.common.exception.BusinessErrorCode;
import vn.cinema.domain.common.exception.InvalidBookingStatusException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingTests {

    @Test
    void appliesValidBookingStatusTransitions() {
        Instant now = Instant.parse("2026-07-23T12:00:00Z");
        Booking expiringBooking = Booking.builder()
                .status(BookingStatus.PENDING)
                .expiresAt(now)
                .build();
        Booking confirmingBooking = Booking.builder().status(BookingStatus.PENDING).build();
        Booking cancellingPendingBooking = Booking.builder().status(BookingStatus.PENDING).build();
        Booking cancellingConfirmedBooking = Booking.builder().status(BookingStatus.CONFIRMED).build();

        expiringBooking.expire(now);
        confirmingBooking.confirm();
        cancellingPendingBooking.cancel();
        cancellingConfirmedBooking.cancel();

        assertEquals(BookingStatus.EXPIRED, expiringBooking.getStatus());
        assertEquals(BookingStatus.CONFIRMED, confirmingBooking.getStatus());
        assertEquals(BookingStatus.CANCELLED, cancellingPendingBooking.getStatus());
        assertEquals(BookingStatus.CANCELLED, cancellingConfirmedBooking.getStatus());
    }

    @Test
    void rejectsInvalidStatusTransitionWithStableCode() {
        Booking booking = Booking.builder()
                .status(BookingStatus.CONFIRMED)
                .expiresAt(Instant.EPOCH)
                .build();

        InvalidBookingStatusException exception =
                assertThrows(InvalidBookingStatusException.class, booking::expire);

        assertEquals(BusinessErrorCode.INVALID_BOOKING_STATUS.name(), exception.getCode());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    void rejectsExpirationBeforeDeadline() {
        Instant now = Instant.parse("2026-07-23T12:00:00Z");
        Booking booking = Booking.builder()
                .status(BookingStatus.PENDING)
                .expiresAt(now.plusSeconds(1))
                .build();

        BookingExpirationNotReachedException exception =
                assertThrows(BookingExpirationNotReachedException.class, () -> booking.expire(now));

        assertEquals(BusinessErrorCode.BOOKING_EXPIRATION_NOT_REACHED.name(), exception.getCode());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
    }
}
