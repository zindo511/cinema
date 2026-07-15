package vn.cinema.domain.showtime.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.cinema.domain.cinema.entity.Seat;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "showtime_seat", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"showtime_id", "seat_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowtimeSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "status", nullable = false)
    private ShowtimeSeatStatus status;

    @Column(name = "price", nullable = false, precision = 10)
    private BigDecimal price;

    @Column(name = "held_at")
    private Instant heldAt;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 0;
}
