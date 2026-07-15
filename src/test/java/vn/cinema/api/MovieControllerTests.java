package vn.cinema.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.cinema.app.dto.MovieDetailResponse;
import vn.cinema.app.dto.MovieListResponse;
import vn.cinema.app.dto.ShowtimeResponse;
import vn.cinema.app.service.MovieService;
import vn.cinema.app.service.ShowtimeService;
import vn.cinema.domain.movie.entity.MovieStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MovieControllerTests {

    @Mock
    private MovieService movieService;

    @Mock
    private ShowtimeService showtimeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MovieController(movieService, showtimeService))
                .build();
    }

    @Test
    void returnsNowShowingMoviesWithOptionalFilters() throws Exception {
        MovieListResponse response = MovieListResponse.builder()
                .id(1L)
                .title("Interstellar")
                .posterUrl("https://example.com/poster.jpg")
                .durationMinutes(169)
                .rating(new BigDecimal("8.7"))
                .genres(Set.of("Sci-Fi"))
                .build();

        when(movieService.getNowShowingMovies("Sci-Fi", "inter"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/movies")
                        .param("genre", "Sci-Fi")
                        .param("keyword", "inter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Interstellar"))
                .andExpect(jsonPath("$[0].posterUrl").value("https://example.com/poster.jpg"))
                .andExpect(jsonPath("$[0].durationMinutes").value(169))
                .andExpect(jsonPath("$[0].rating").value(8.7))
                .andExpect(jsonPath("$[0].genres[0]").value("Sci-Fi"));
    }

    @Test
    void createsMovieWithGenreIds() throws Exception {
        MovieDetailResponse response = MovieDetailResponse.builder()
                .id(1L)
                .title("Interstellar")
                .status(MovieStatus.COMING_SOON)
                .genres(Set.of("Sci-Fi"))
                .build();

        when(movieService.createMovie(org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Interstellar",
                                  "durationMinutes": 169,
                                  "genreIds": [1]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("COMING_SOON"))
                .andExpect(jsonPath("$.genres[0]").value("Sci-Fi"));
    }

    @Test
    void patchesMovie() throws Exception {
        MovieDetailResponse response = MovieDetailResponse.builder()
                .id(1L)
                .title("Interstellar 2")
                .durationMinutes(170)
                .status(MovieStatus.COMING_SOON)
                .build();

        when(movieService.updateMovie(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/movies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Interstellar 2",
                                  "durationMinutes": 170
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Interstellar 2"))
                .andExpect(jsonPath("$.durationMinutes").value(170));
    }

    @Test
    void changesMovieStatus() throws Exception {
        MovieDetailResponse response = MovieDetailResponse.builder()
                .id(1L)
                .title("Interstellar")
                .status(MovieStatus.NOW_SHOWING)
                .build();

        when(movieService.changeMovieStatus(1L, MovieStatus.NOW_SHOWING))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/movies/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "NOW_SHOWING"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOW_SHOWING"));
    }

    @Test
    void softDeletesMovie() throws Exception {
        MovieDetailResponse response = MovieDetailResponse.builder()
                .id(1L)
                .title("Interstellar")
                .status(MovieStatus.DELETED)
                .build();

        when(movieService.softDeleteMovie(1L)).thenReturn(response);

        mockMvc.perform(delete("/api/v1/movies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELETED"));
    }

    @Test
    void returnsAvailableShowtimesForMovieCinemaAndDate() throws Exception {
        ShowtimeResponse response = ShowtimeResponse.builder()
                .id(11L)
                .startTime(Instant.parse("2026-07-13T05:00:00Z"))
                .auditoriumName("Room 1")
                .basePrice(new BigDecimal("90000"))
                .availableSeats(42L)
                .build();

        when(showtimeService.getAvailableShowtimes(
                1L,
                2L,
                LocalDate.of(2026, 7, 13)
        )).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/movies/1/showtimes")
                        .param("cinemaId", "2")
                        .param("date", "2026-07-13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].startTime").value("2026-07-13T05:00:00Z"))
                .andExpect(jsonPath("$[0].auditoriumName").value("Room 1"))
                .andExpect(jsonPath("$[0].basePrice").value(90000))
                .andExpect(jsonPath("$[0].availableSeats").value(42));
    }
}
