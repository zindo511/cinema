package vn.cinema.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.cinema.app.dto.request.CreateAuditoriumRequest;
import vn.cinema.app.dto.request.CreateCinemaRequest;
import vn.cinema.app.dto.request.UpdateSeatLayoutRequest;
import vn.cinema.app.dto.response.AuditoriumDetailResponse;
import vn.cinema.app.dto.response.CinemaDetailResponse;
import vn.cinema.app.service.CinemaService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;

    // ==================== Cinema ====================

    @PostMapping("/cinemas")
    public ResponseEntity<CinemaDetailResponse> createCinema(
            @Valid @RequestBody CreateCinemaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cinemaService.createCinema(request));
    }

    // ==================== Auditorium ====================

    @PostMapping("/auditoriums")
    public ResponseEntity<AuditoriumDetailResponse> createAuditorium(
            @Valid @RequestBody CreateAuditoriumRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cinemaService.createAuditorium(request));
    }

    // ==================== Seat Layout ====================

    @PutMapping("/auditoriums/{auditoriumId}/seats")
    public ResponseEntity<AuditoriumDetailResponse> updateSeatLayout(
            @PathVariable Long auditoriumId,
            @Valid @RequestBody UpdateSeatLayoutRequest request) {
        return ResponseEntity.ok(cinemaService.updateSeatLayout(auditoriumId, request));
    }
}
