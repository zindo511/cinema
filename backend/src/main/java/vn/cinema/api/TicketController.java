package vn.cinema.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.cinema.app.dto.response.CheckInResponse;
import vn.cinema.app.dto.response.MyTicketResponse;
import vn.cinema.app.service.TicketService;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PostMapping("/{ticketCode}/check-in")
    public ResponseEntity<CheckInResponse> handleCheckIn(
            @PathVariable String ticketCode,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = ((Number) Objects.requireNonNull(jwt.getClaim("userId"))).longValue();
        return ResponseEntity.status(HttpStatus.OK).body(ticketService.handleCheckIn(ticketCode, userId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<MyTicketResponse>> getMyTickets(@AuthenticationPrincipal Jwt jwt) {
        Long userId = ((Number) Objects.requireNonNull(jwt.getClaim("userId"))).longValue();
        return ResponseEntity.ok(ticketService.getMyTickets(userId));
    }
}
