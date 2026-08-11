package vn.cinema.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCinemaRequest {

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
}
