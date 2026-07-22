package vn.cinema.api;

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
import vn.cinema.app.dto.request.CreateCinemaRequest;
import vn.cinema.app.dto.response.CinemaDetailResponse;
import vn.cinema.app.service.CinemaService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CinemaControllerSecurityTests.TestConfig.class)
class CinemaControllerSecurityTests {

    @Autowired
    private CinemaController cinemaController;

    @Autowired
    private CinemaService cinemaService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void allowsAdminToCreateCinema() {
        CinemaDetailResponse response = CinemaDetailResponse.builder()
                .id(1L)
                .name("Cinema One")
                .build();
        when(cinemaService.createCinema(any(CreateCinemaRequest.class))).thenReturn(response);

        var result = cinemaController.createCinema(createCinemaRequest());

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deniesRegularUserFromCreatingCinema() {
        assertThrows(
                AccessDeniedException.class,
                () -> cinemaController.createCinema(createCinemaRequest())
        );
        verifyNoInteractions(cinemaService);
    }

    private CreateCinemaRequest createCinemaRequest() {
        return CreateCinemaRequest.builder()
                .name("Cinema One")
                .city("Ho Chi Minh City")
                .address("1 Nguyen Hue")
                .build();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        CinemaService cinemaService() {
            return mock(CinemaService.class);
        }

        @Bean
        CinemaController cinemaController(CinemaService cinemaService) {
            return new CinemaController(cinemaService);
        }
    }
}
