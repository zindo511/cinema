package vn.cinema.domain.cinema.entity;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum SeatType {
    STANDARD(0, BigDecimal.valueOf(1.0)),
    VIP(1, BigDecimal.valueOf(1.5)),
    COUPLE(2, BigDecimal.valueOf(2.0));

    private final int value;
    private final BigDecimal priceMultiplier;

    SeatType(int value, BigDecimal priceMultiplier) {
        this.value = value;
        this.priceMultiplier = priceMultiplier;
    }

    public static SeatType fromValue(int value) {
        for (SeatType type : SeatType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown SeatType value: " + value);
    }
}
