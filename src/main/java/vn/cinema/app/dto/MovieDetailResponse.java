package vn.cinema.app.dto;

import lombok.*;
import vn.cinema.domain.movie.entity.MovieStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDetailResponse {
    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private String posterUrl;
    private String trailerUrl;
    private LocalDate releaseDate;
    private BigDecimal rating;
    private MovieStatus status;
    private Set<String> genres;
    private List<CinemaDto> cinemas;
}
