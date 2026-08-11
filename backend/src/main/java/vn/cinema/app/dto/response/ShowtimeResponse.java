package vn.cinema.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeResponse {

    private Long id;
    private Instant startTime;
    private String auditoriumName;
    private BigDecimal basePrice;
    private Long availableSeats;
}
