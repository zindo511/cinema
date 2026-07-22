package vn.cinema.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.cinema.domain.showtime.entity.ShowtimeStatus;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeSeatMapResponse {

    private Long showtimeId;
    private ShowtimeStatus status;
    private Instant startTime;

    @Builder.Default
    private List<ShowtimeSeatResponse> seats = List.of();
}
