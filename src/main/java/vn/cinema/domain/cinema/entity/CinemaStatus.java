package vn.cinema.domain.cinema.entity;

import lombok.Getter;

@Getter
public enum CinemaStatus {
    DELETED(-1),
    INACTIVE(0),
    ACTIVE(1);

    private final int value;

    CinemaStatus(int value) {
        this.value = value;
    }

    public static CinemaStatus fromValue(int value) {
        for (CinemaStatus status : CinemaStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown CinemaStatus value: " + value);
    }
}
