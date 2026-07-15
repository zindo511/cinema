package vn.cinema.domain.cinema.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.cinema.domain.common.entity.BaseAuditEntity;

@Entity
@Table(name = "auditorium", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cinema_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditorium extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "screen_type", nullable = false)
    private ScreenType screenType;

    @Column(name = "total_rows", nullable = false)
    private Integer totalRows;

    @Column(name = "total_columns", nullable = false)
    private Integer totalColumns;

    @Column(name = "status", nullable = false)
    private AuditoriumStatus status;
}
