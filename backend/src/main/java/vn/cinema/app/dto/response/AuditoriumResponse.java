package vn.cinema.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import vn.cinema.domain.cinema.entity.AuditoriumStatus;
import vn.cinema.domain.cinema.entity.ScreenType;

@Getter
@Builder
@AllArgsConstructor
public class AuditoriumResponse {

    private Long id;
    private Long cinemaId;
    private String name;
    private ScreenType screenType;
    private Integer totalRows;
    private Integer totalColumns;
    private AuditoriumStatus status;
}
