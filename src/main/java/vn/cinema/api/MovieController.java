package vn.cinema.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.cinema.app.dto.request.ChangeMovieStatusRequest;
import vn.cinema.app.dto.request.CreateMovieRequest;
import vn.cinema.app.dto.request.UpdateMovieRequest;
import vn.cinema.app.dto.response.MovieDetailResponse;
import vn.cinema.app.dto.response.MovieListResponse;
import vn.cinema.app.dto.response.ShowtimeResponse;
import vn.cinema.app.service.MovieService;
import vn.cinema.app.service.ShowtimeService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final ShowtimeService showtimeService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MovieDetailResponse> createMovie(@Valid @RequestBody CreateMovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.createMovie(request));
    }

    @GetMapping
    public ResponseEntity<List<MovieListResponse>> getNowShowingMovies(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(movieService.getNowShowingMovies(genre, keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDetailResponse> getMovieDetails(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieDetails(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<MovieDetailResponse> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMovieRequest request
    ) {
        return ResponseEntity.ok(movieService.updateMovie(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<MovieDetailResponse> changeMovieStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeMovieStatusRequest request
    ) {
        return ResponseEntity.ok(movieService.changeMovieStatus(id, request.getStatus()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MovieDetailResponse> softDeleteMovie(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.softDeleteMovie(id));
    }

    @GetMapping("/{movieId}/showtimes")
    public ResponseEntity<List<ShowtimeResponse>> getAvailableShowtimes(
            @PathVariable Long movieId,
            @RequestParam Long cinemaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(showtimeService.getAvailableShowtimes(movieId, cinemaId, date));
    }
}
