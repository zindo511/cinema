package vn.cinema.app.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class UpdateShowtimeRequest {

    @Positive(message = "Movie ID must be positive")
    private Long movieId;

    @Positive(message = "Auditorium ID must be positive")
    private Long auditoriumId;

    @Future(message = "Start time must be in the future")
    private Instant startTime;

    @Positive(message = "Base price must be positive")
    @Digits(
            integer = 10,
            fraction = 0,
            message = "Base price must contain at most 10 digits and no decimal places"
    )
    private BigDecimal basePrice;
}
