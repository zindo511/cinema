package vn.cinema.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import vn.cinema.app.dto.request.ChangeMovieStatusRequest;
import vn.cinema.app.dto.request.CreateMovieRequest;
import vn.cinema.app.dto.request.UpdateMovieRequest;
import vn.cinema.app.dto.response.MovieDetailResponse;
import vn.cinema.app.service.MovieService;
import vn.cinema.app.service.ShowtimeService;
import vn.cinema.domain.movie.entity.MovieStatus;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MovieControllerSecurityTests.TestConfig.class)
class MovieControllerSecurityTests {

    @Autowired
    private MovieController movieController;

    @Autowired
    private MovieService movieService;

    @Autowired
    private ShowtimeService showtimeService;

    @BeforeEach
    void resetMocks() {
        reset(movieService, showtimeService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void allowsAdminToCreateMovie() {
        MovieDetailResponse response = MovieDetailResponse.builder()
                .id(1L)
                .title("Interstellar")
                .build();
        when(movieService.createMovie(any(CreateMovieRequest.class))).thenReturn(response);

        var result = movieController.createMovie(createMovieRequest());

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deniesRegularUserFromManagingMovies() {
        assertThrows(AccessDeniedException.class,
                () -> movieController.createMovie(createMovieRequest()));
        assertThrows(AccessDeniedException.class,
                () -> movieController.updateMovie(1L, new UpdateMovieRequest()));
        assertThrows(AccessDeniedException.class,
                () -> movieController.changeMovieStatus(
                        1L,
                        new ChangeMovieStatusRequest(MovieStatus.NOW_SHOWING)
                ));
        assertThrows(AccessDeniedException.class,
                () -> movieController.softDeleteMovie(1L));

        verifyNoInteractions(movieService, showtimeService);
    }

    private CreateMovieRequest createMovieRequest() {
        return CreateMovieRequest.builder()
                .title("Interstellar")
                .durationMinutes(169)
                .genreIds(Set.of(1L))
                .build();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        MovieService movieService() {
            return mock(MovieService.class);
        }

        @Bean
        ShowtimeService showtimeService() {
            return mock(ShowtimeService.class);
        }

        @Bean
        MovieController movieController(
                MovieService movieService,
                ShowtimeService showtimeService
        ) {
            return new MovieController(movieService, showtimeService);
        }
    }
}
