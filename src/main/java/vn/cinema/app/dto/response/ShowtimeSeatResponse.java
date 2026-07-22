package vn.cinema.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.cinema.domain.cinema.entity.SeatType;
import vn.cinema.domain.showtime.entity.ShowtimeSeatStatus;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeSeatResponse {

    private Long showtimeSeatId;
    private String label;
    private SeatType type;
    private BigDecimal price;
    private ShowtimeSeatStatus status;
}
