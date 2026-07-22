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
import vn.cinema.app.dto.request.CreateShowtimeRequest;
import vn.cinema.app.dto.response.ShowtimeDetailResponse;
import vn.cinema.app.service.ShowtimeService;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ShowtimeControllerSecurityTests.TestConfig.class)
class ShowtimeControllerSecurityTests {

    @Autowired
    private ShowtimeController showtimeController;

    @Autowired
    private ShowtimeService showtimeService;

    @BeforeEach
    void resetMock() {
        reset(showtimeService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void allowsAdminToCreateShowtime() {
        ShowtimeDetailResponse response = ShowtimeDetailResponse.builder()
                .id(1L)
                .movieId(2L)
                .auditoriumId(3L)
                .status("DRAFT")
                .build();
        when(showtimeService.createShowtime(any(CreateShowtimeRequest.class))).thenReturn(response);

        var result = showtimeController.createShowtime(createShowtimeRequest());

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deniesRegularUserFromManagingShowtimes() {
        assertThrows(AccessDeniedException.class,
                () -> showtimeController.createShowtime(createShowtimeRequest()));
        assertThrows(AccessDeniedException.class,
                () -> showtimeController.approveShowtime(1L));
        assertThrows(AccessDeniedException.class,
                () -> showtimeController.cancelShowtime(1L));

        verifyNoInteractions(showtimeService);
    }

    private CreateShowtimeRequest createShowtimeRequest() {
        return new CreateShowtimeRequest(
                2L,
                3L,
                Instant.parse("2026-07-23T10:00:00Z"),
                new BigDecimal("90000")
        );
    }

    @Configuration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        ShowtimeService showtimeService() {
            return mock(ShowtimeService.class);
        }

        @Bean
        ShowtimeController showtimeController(ShowtimeService showtimeService) {
            return new ShowtimeController(showtimeService);
        }
    }
}
