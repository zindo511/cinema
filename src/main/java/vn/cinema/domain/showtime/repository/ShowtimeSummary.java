package vn.cinema.domain.showtime.repository;

import java.math.BigDecimal;
import java.time.Instant;

public interface ShowtimeSummary {

    Long getId();

    Instant getStartTime();

    String getAuditoriumName();

    BigDecimal getBasePrice();

    Long getAvailableSeats();
}
