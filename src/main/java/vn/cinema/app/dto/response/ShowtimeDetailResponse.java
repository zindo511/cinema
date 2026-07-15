package vn.cinema.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeDetailResponse {

    private Long id;
    private Long movieId;
    private String movieTitle;
    private Long auditoriumId;
    private String auditoriumName;
    private Instant startTime;
    private Instant endTime;
    private Instant cleanupUntil;
    private BigDecimal basePrice;
    private String status;
    private int totalSeats;
}
