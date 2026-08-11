package vn.cinema.app.dto.response;

import lombok.*;
import vn.cinema.domain.cinema.entity.AuditoriumStatus;
import vn.cinema.domain.cinema.entity.ScreenType;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriumDetailResponse {
    private Long id;
    private Long cinemaId;
    private String cinemaName;
    private String name;
    private ScreenType screenType;
    private Integer totalRows;
    private Integer totalColumns;
    private AuditoriumStatus status;
    private List<SeatResponse> seats;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatResponse {
        private Long id;
        private String seatRow;
        private Integer seatNumber;
        private String seatType;
        private Boolean isActive;
    }
}
