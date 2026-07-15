package vn.cinema.app.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMovieRequest {

    @Size(max = 255)
    private String title;

    private String description;

    @Positive
    private Integer durationMinutes;

    @Size(max = 500)
    private String posterUrl;

    @Size(max = 500)
    private String trailerUrl;

    private LocalDate releaseDate;

    private BigDecimal rating;

    private Set<Long> genreIds;
}
