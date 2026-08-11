package vn.cinema.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CinemaShowtimesResponse {

    private Long movieId;
    private String title;
    private String posterUrl;
    private Integer duration;
    private String rated;
    private List<ShowtimeResponse> showtimes;
}
