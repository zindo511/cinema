package vn.cinema.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.cinema.domain.booking.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private String bookingCode;
    private Long showtimeId;
    private BookingStatus status;

    @Builder.Default
    private List<BookingSeatResponse> seats = List.of();

    private BigDecimal totalAmount;
    private Instant expiresAt;
}
