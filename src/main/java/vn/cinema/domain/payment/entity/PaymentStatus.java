package vn.cinema.domain.payment.entity;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    FAILED(-1),
    PENDING(0),
    SUCCESS(1),
    REFUND_PENDING(2);

    private final int value;

    PaymentStatus(int value) {
        this.value = value;
    }

    public static PaymentStatus fromValue(int value) {
        for (PaymentStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown PaymentStatus value: " + value);
    }
}

