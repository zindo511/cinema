package vn.cinema.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.cinema.app.dto.MovieListResponse;
import vn.cinema.app.mapper.CinemaMapper;
import vn.cinema.app.mapper.MovieMapper;
import vn.cinema.domain.cinema.repository.CinemaRepository;
import vn.cinema.domain.movie.entity.Movie;
import vn.cinema.domain.movie.entity.MovieStatus;
import vn.cinema.domain.movie.port.MovieBookingPort;
import vn.cinema.domain.movie.repository.GenreRepository;
import vn.cinema.domain.movie.repository.MovieRepository;
import vn.cinema.domain.showtime.entity.ShowtimeStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTests {

    private static final ZoneId CINEMA_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Instant NOW = Instant.parse("2026-07-13T03:00:00Z");

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private MovieBookingPort movieBookingPort;

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private CinemaMapper cinemaMapper;

    private MovieService movieService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, CINEMA_ZONE);
        movieService = new MovieService(
                movieRepository,
                genreRepository,
                movieBookingPort,
                cinemaRepository,
                movieMapper,
                cinemaMapper,
                clock
        );
    }

    @Test
    void returnsNowShowingMoviesUsingFutureOpenShowtimeRuleAndFilters() {
        Movie movie = mock(Movie.class);
        MovieListResponse response = MovieListResponse.builder()
                .id(1L)
                .title("Interstellar")
                .build();

        when(movieRepository.findNowShowingMovies(
                NOW,
                MovieStatus.NOW_SHOWING,
                ShowtimeStatus.OPEN,
                "Sci-Fi",
                "inter"
        )).thenReturn(List.of(movie));
        when(movieMapper.toListResponse(movie)).thenReturn(response);

        List<MovieListResponse> result = movieService.getNowShowingMovies(" Sci-Fi ", " inter ");

        assertThat(result).containsExactly(response);
    }

    @Test
    void changesComingSoonMovieToNowShowing() {
        Movie movie = Movie.builder()
                .id(1L)
                .title("Interstellar")
                .status(MovieStatus.COMING_SOON)
                .build();

        when(movieRepository.findByIdWithGenres(1L)).thenReturn(Optional.of(movie));

        movieService.changeMovieStatus(1L, MovieStatus.NOW_SHOWING);

        assertThat(movie.getStatus()).isEqualTo(MovieStatus.NOW_SHOWING);
    }

    @Test
    void rejectsInvalidMovieStatusTransition() {
        Movie movie = Movie.builder()
                .id(1L)
                .title("Interstellar")
                .status(MovieStatus.ENDED)
                .build();

        when(movieRepository.findByIdWithGenres(1L)).thenReturn(Optional.of(movie));

        assertThatThrownBy(() -> movieService.changeMovieStatus(1L, MovieStatus.NOW_SHOWING))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("COMING_SOON");
    }

    @Test
    void softDeletesMovieWithoutConfirmedBookings() {
        Movie movie = Movie.builder()
                .id(1L)
                .title("Interstellar")
                .status(MovieStatus.NOW_SHOWING)
                .build();

        when(movieRepository.findByIdWithGenres(1L)).thenReturn(Optional.of(movie));
        when(movieBookingPort.hasConfirmedBookings(1L)).thenReturn(false);

        movieService.softDeleteMovie(1L);

        assertThat(movie.getStatus()).isEqualTo(MovieStatus.DELETED);
    }

    @Test
    void rejectsSoftDeleteWhenMovieHasConfirmedBookings() {
        Movie movie = Movie.builder()
                .id(1L)
                .title("Interstellar")
                .status(MovieStatus.NOW_SHOWING)
                .build();

        when(movieRepository.findByIdWithGenres(1L)).thenReturn(Optional.of(movie));
        when(movieBookingPort.hasConfirmedBookings(1L)).thenReturn(true);

        assertThatThrownBy(() -> movieService.softDeleteMovie(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CONFIRMED bookings");
    }
}
