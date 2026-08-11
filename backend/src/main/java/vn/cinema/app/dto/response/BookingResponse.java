package vn.cinema.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.cinema.domain.booking.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;
    private String bookingCode;
    private Long showtimeId;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private Instant expiresAt;
    private List<BookingSeatResponse> seats;
}
