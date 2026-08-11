package vn.cinema.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import vn.cinema.domain.cinema.entity.ScreenType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuditoriumRequest {

    @NotNull
    private Long cinemaId;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private ScreenType screenType;

    @NotNull
    @Positive
    private Integer totalRows;

    @NotNull
    @Positive
    private Integer totalColumns;
}
