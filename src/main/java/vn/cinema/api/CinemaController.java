package vn.cinema.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.cinema.app.dto.request.CreateCinemaRequest;
import vn.cinema.app.dto.response.CinemaDetailResponse;
import vn.cinema.app.service.CinemaService;

import java.util.List;

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
}
