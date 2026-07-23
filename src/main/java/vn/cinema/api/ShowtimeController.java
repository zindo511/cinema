package vn.cinema.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.cinema.app.dto.request.CreateShowtimeRequest;
import vn.cinema.app.dto.response.ShowtimeDetailResponse;
import vn.cinema.app.dto.response.ShowtimeResponse;
import vn.cinema.app.dto.response.ShowtimeSeatMapResponse;
import vn.cinema.app.service.ShowtimeService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ShowtimeDetailResponse> createShowtime(
            @Valid @RequestBody CreateShowtimeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(showtimeService.createShowtime(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ShowtimeDetailResponse> approveShowtime(@PathVariable Long id) {
        return ResponseEntity.ok(showtimeService.approveShowtime(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ShowtimeDetailResponse> cancelShowtime(@PathVariable Long id) {
        return ResponseEntity.ok(showtimeService.cancelShowtime(id));
    }

    @GetMapping("/{showtimeId}/seats")
    public ResponseEntity<ShowtimeSeatMapResponse> viewSeatMap(@PathVariable Long showtimeId) {
        return ResponseEntity.ok(showtimeService.viewSeatMap(showtimeId));
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
