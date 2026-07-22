package vn.cinema.domain.user.entity;

import lombok.Getter;

@Getter
public enum UserStatus {
    DELETED(-1),
    ACTIVE(0),
    LOCKED(1);

    private final int value;

    UserStatus(int value) {
        this.value = value;
    }

    public static UserStatus fromValue(int value) {
        for (UserStatus status : UserStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown UserStatus value: " + value);
    }
}
