package vn.cinema.domain.movie.entity;

import lombok.Getter;

@Getter
public enum MovieStatus {
    DELETED(-1),
    COMING_SOON(0),
    NOW_SHOWING(1),
    ENDED(2);

    private final int value;

    MovieStatus(int value) {
        this.value = value;
    }

    public static MovieStatus fromValue(int value) {
        for (MovieStatus status : MovieStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown MovieStatus value: " + value);
    }
}
