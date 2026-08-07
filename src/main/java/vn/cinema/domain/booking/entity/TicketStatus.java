package vn.cinema.domain.booking.entity;

import lombok.Getter;

@Getter
public enum TicketStatus {
    ISSUED(1),
    USED(2);

    private final int value;

    TicketStatus(int value) {
        this.value = value;
    }

    public static TicketStatus fromValue(int value) {
        for (TicketStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown TicketStatus value: " + value);
    }
}
