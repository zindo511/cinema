package vn.cinema.app.adapter.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.cinema.domain.showtime.port.ShowtimeRefundPort;

/**
 * Stub adapter: no booking domain yet, so refund is a no-op.
 * Replace with real implementation when booking module is integrated.
 */
@Slf4j
@Component
public class NoOpShowtimeRefundAdapter implements ShowtimeRefundPort {

    @Override
    public void refundAllBookings(Long showtimeId) {
        log.warn("Refund requested for showtime {} — booking module not yet implemented, skipping.", showtimeId);
    }
}
