package vn.cinema.app.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateShowtimeRequest {

    @NotNull(message = "Movie ID is required")
    private Long movieId;

    @NotNull(message = "Auditorium ID is required")
    private Long auditoriumId;

    @NotNull(message = "Start time is required")
    private Instant startTime;

    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;
}
