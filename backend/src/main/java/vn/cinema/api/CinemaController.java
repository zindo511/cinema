package vn.cinema.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.cinema.app.dto.request.CreateCinemaRequest;
import vn.cinema.app.dto.request.UpdateCinemaRequest;
import vn.cinema.app.dto.response.AuditoriumResponse;
import vn.cinema.app.dto.response.CinemaDetailResponse;
import vn.cinema.app.dto.response.CinemaShowtimesResponse;
import vn.cinema.app.service.CinemaService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CinemaDetailResponse> createCinema(
            @Valid @RequestBody CreateCinemaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cinemaService.createCinema(request));
    }

    @GetMapping("/cities")
    public ResponseEntity<List<String>> getCities() {
        return ResponseEntity.ok(cinemaService.getCityNames());
    }

    @GetMapping
    public ResponseEntity<List<CinemaDetailResponse>> getCinemas(@RequestParam(required = false) String city) {
        return ResponseEntity.ok(cinemaService.getCinemas(city));
    }

    @GetMapping("/{cinemaId}")
    public ResponseEntity<CinemaDetailResponse> getDetailsCinema(@PathVariable Long cinemaId) {
        return ResponseEntity.ok(cinemaService.getDetailsCinema(cinemaId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{cinemaId}/update")
    public ResponseEntity<CinemaDetailResponse> updateCinema(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long cinemaId,
            @Valid @RequestBody UpdateCinemaRequest request
    ) {
        Long userId = ((Number) Objects.requireNonNull(
                jwt.getClaim("userId")
        )).longValue();

        return ResponseEntity.ok(cinemaService.updateCinema(cinemaId, request, userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{cinemaId}")
    public ResponseEntity<Void> deleteCinema(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long cinemaId
    ) {
        Long userId = ((Number) Objects.requireNonNull(
                jwt.getClaim("userId")
        )).longValue();
        cinemaService.deleteCinema(cinemaId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{cinemaId}/auditoriums")
    public ResponseEntity<List<AuditoriumResponse>> getAuditoriums(
            @PathVariable Long cinemaId
    ) {
        return ResponseEntity.ok(cinemaService.getAuditoriums(cinemaId));
    }

    @GetMapping("/{cinemaId}/showtimes")
    public ResponseEntity<List<CinemaShowtimesResponse>> getShowtimes(
            @PathVariable Long cinemaId,
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(cinemaService.getShowtimsByCinemaAndDate(cinemaId, date));
    }
}
