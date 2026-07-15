package vn.cinema.domain.cinema.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.cinema.domain.common.entity.BaseAuditEntity;

@Entity
@Table(name = "cinema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cinema extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "status", nullable = false)
    private CinemaStatus status;
}
