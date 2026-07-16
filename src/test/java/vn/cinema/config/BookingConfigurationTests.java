package vn.cinema.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(BookingConfiguration.class)
            .withPropertyValues("cinema.booking.hold-duration=10m");

    @Test
    void bindsBookingHoldDuration() {
        contextRunner.run(context -> {
            BookingProperties properties = context.getBean(BookingProperties.class);
            assertEquals(Duration.ofMinutes(10), properties.holdDuration());
        });
    }
}
