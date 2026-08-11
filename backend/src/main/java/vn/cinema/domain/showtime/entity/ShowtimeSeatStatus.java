package vn.cinema.domain.showtime.entity;

import lombok.Getter;

@Getter
public enum ShowtimeSeatStatus {
    AVAILABLE(0),
    HELD(1),
    BOOKED(2);

    private final int value;

    ShowtimeSeatStatus(int value) {
        this.value = value;
    }

    public static ShowtimeSeatStatus fromValue(int value) {
        for (ShowtimeSeatStatus status : ShowtimeSeatStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ShowtimeSeatStatus value: " + value);
    }
}
