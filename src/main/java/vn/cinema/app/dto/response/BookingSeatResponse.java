package vn.cinema.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import vn.cinema.domain.cinema.entity.SeatType;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class BookingSeatResponse {

    private Long showtimeSeatId;
    private String seatLabel;
    private SeatType seatType;
    private BigDecimal price;
}
