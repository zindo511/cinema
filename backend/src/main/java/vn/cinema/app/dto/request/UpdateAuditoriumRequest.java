package vn.cinema.app.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.cinema.domain.cinema.entity.AuditoriumStatus;
import vn.cinema.domain.cinema.entity.ScreenType;

@Getter
@Setter
@NoArgsConstructor
public class UpdateAuditoriumRequest {

    @Size(max = 100)
    private String name;

    private ScreenType screenType;

    private AuditoriumStatus status;
}
