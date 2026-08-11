package vn.cinema.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.cinema.domain.cinema.entity.CinemaStatus;

@Getter
@Setter
@NoArgsConstructor
public class UpdateCinemaRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String district;

    @NotBlank
    @Size(max = 500)
    private String address;

    @Size(max = 20)
    private String phone;

    @NotNull
    private CinemaStatus status;
}
