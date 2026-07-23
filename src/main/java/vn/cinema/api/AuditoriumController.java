package vn.cinema.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.cinema.app.dto.request.CreateAuditoriumRequest;
import vn.cinema.app.dto.request.UpdateSeatLayoutRequest;
import vn.cinema.app.dto.response.AuditoriumDetailResponse;
import vn.cinema.app.service.CinemaService;

@RestController
@RequestMapping("/api/v1/auditoriums")
@RequiredArgsConstructor
public class AuditoriumController {

    private final CinemaService cinemaService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AuditoriumDetailResponse> createAuditorium(
            @Valid @RequestBody CreateAuditoriumRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cinemaService.createAuditorium(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{auditoriumId}/seats")
    public ResponseEntity<AuditoriumDetailResponse> updateSeatLayout(
            @PathVariable Long auditoriumId,
            @Valid @RequestBody UpdateSeatLayoutRequest request) {
        return ResponseEntity.ok(cinemaService.updateSeatLayout(auditoriumId, request));
    }
}
