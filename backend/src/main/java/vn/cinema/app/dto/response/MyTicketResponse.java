package vn.cinema.app.dto.response;

import lombok.Builder;
import vn.cinema.domain.booking.entity.TicketStatus;

import java.time.Instant;

@Builder
public record MyTicketResponse(
        String ticketCode,
        TicketStatus status,
        String movieTitle,
        String posterUrl,
        String cinemaName,
        String auditoriumName,
        Instant showtimeStartTime,
        String seatLabel,
        String seatType
) {
}
