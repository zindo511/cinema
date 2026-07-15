package vn.cinema.domain.showtime.entity;

import lombok.Getter;

@Getter
public enum ShowtimeStatus {
    CANCELLED(-1),
    DRAFT(0),
    OPEN(1),
    COMPLETED(2);

    private final int value;

    ShowtimeStatus(int value) {
        this.value = value;
    }

    public static ShowtimeStatus fromValue(int value) {
        for (ShowtimeStatus status : ShowtimeStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ShowtimeStatus value: " + value);
    }
}
