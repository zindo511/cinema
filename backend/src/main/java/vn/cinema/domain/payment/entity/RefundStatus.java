package vn.cinema.domain.payment.entity;

import lombok.Getter;

@Getter
public enum RefundStatus {
    FAILED(-1),
    PENDING(0),
    SUCCESS(1);

    private final int value;

    RefundStatus(int value) {
        this.value = value;
    }

    public static RefundStatus fromValue(int value) {
        for (RefundStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown RefundStatus value: " + value);
    }
}
