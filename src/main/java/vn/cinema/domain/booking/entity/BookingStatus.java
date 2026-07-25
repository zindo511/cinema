package vn.cinema.domain.booking.entity;

import lombok.Getter;

@Getter
public enum BookingStatus {
    CANCELLED(-1),
    PENDING(0),
    CONFIRMED(1),
    EXPIRED(2);

    private final int value;

    BookingStatus(int value) {
        this.value = value;
    }

    public static BookingStatus fromValue(int value) {
        for (BookingStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown BookingStatus value: " + value);
    }
}
