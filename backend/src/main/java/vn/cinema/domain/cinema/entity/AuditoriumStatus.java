package vn.cinema.domain.cinema.entity;

import lombok.Getter;

@Getter
public enum AuditoriumStatus {
    DELETED(-1),
    MAINTENANCE(0),
    ACTIVE(1);

    private final int value;

    AuditoriumStatus(int value) {
        this.value = value;
    }

    public static AuditoriumStatus fromValue(int value) {
        for (AuditoriumStatus status : AuditoriumStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AuditoriumStatus value: " + value);
    }
}
