package vn.cinema.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieListResponse {

    private Long id;
    private String title;
    private Integer durationMinutes;
    private String posterUrl;
    private BigDecimal rating;
    private Set<String> genres;
}
