package vn.cinema.domain.cinema.entity;

import lombok.Getter;

@Getter
public enum ScreenType {
    SCREEN_2D(0),
    SCREEN_3D(1),
    IMAX(2),
    SCREEN_4DX(3);

    private final int value;

    ScreenType(int value) {
        this.value = value;
    }

    public static ScreenType fromValue(int value) {
        for (ScreenType type : ScreenType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ScreenType value: " + value);
    }
}
