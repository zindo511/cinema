package vn.cinema.domain.cinema.repository;

import java.math.BigDecimal;
import java.time.Instant;

public interface CinemaShowtimeSummary {

    Long getMovieId();
    String getTitle();
    String getPosterUrl();
    Integer getDuration();
    String getRated();

    // build showtime-response
    Long getShowtimeId();
    Instant getStartTime();
    String getAuditoriumName();
    BigDecimal getBasePrice();
    Long getAvailableSeats(); // Do hàm COUNT() trả về
}
