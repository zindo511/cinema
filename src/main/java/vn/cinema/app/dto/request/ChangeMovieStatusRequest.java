package vn.cinema.app.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.cinema.domain.movie.entity.MovieStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeMovieStatusRequest {

    @NotNull
    private MovieStatus status;
}
