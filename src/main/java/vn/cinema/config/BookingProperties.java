package vn.cinema.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Objects;

@ConfigurationProperties(prefix = "cinema.booking")
public record BookingProperties(Duration holdDuration) {

    public BookingProperties {
        Objects.requireNonNull(holdDuration, "cinema.booking.hold-duration must be configured");
        if (holdDuration.isZero() || holdDuration.isNegative()) {
            throw new IllegalArgumentException("cinema.booking.hold-duration must be positive");
        }
    }
}
