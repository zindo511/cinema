package vn.cinema.app.dto.response;

import lombok.Builder;
import vn.cinema.domain.booking.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record BookingHistoryResponse(
        Long id,
        String bookingCode,
        BookingStatus status,
        BigDecimal totalAmount,
        Instant createAt,

        String movieName,
        String moviePosterUrl,
        String cinemaName,
        String roomName,
        Instant showtimeStat,

        String seatNames
) {
}
