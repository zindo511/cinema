package vn.cinema.app.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaDto {
    private Long id;
    private String name;
    private String city;
    private String district;
    private String address;
    private String phone;
}
