package vn.cinema.app.dto.response;

import lombok.*;
import vn.cinema.domain.cinema.entity.CinemaStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaDetailResponse {
    private Long id;
    private String name;
    private String city;
    private String district;
    private String address;
    private String phone;
    private CinemaStatus status;
}
