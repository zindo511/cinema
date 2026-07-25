package vn.cinema.domain.showtime.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.cinema.domain.common.entity.BaseAuditEntity;
import vn.cinema.domain.cinema.entity.Auditorium;
import vn.cinema.domain.common.exception.BusinessErrorCode;
import vn.cinema.domain.common.exception.ConflictException;
import vn.cinema.domain.movie.entity.Movie;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "showtime")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Showtime extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auditorium_id", nullable = false)
    private Auditorium auditorium;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "base_price", nullable = false, precision = 10)
    private BigDecimal basePrice;

    @Column(name = "status", nullable = false)
    private ShowtimeStatus status;

    // ==================== State Machine ====================

    /**
     * DRAFT → OPEN
     */
    public void approve() {
        if (status != ShowtimeStatus.DRAFT) {
            throw new IllegalStateException(
                    "Cannot approve showtime: current status is " + status + ", expected DRAFT");
        }
        this.status = ShowtimeStatus.OPEN;
    }

    /**
     * DRAFT → CANCELLED or OPEN → CANCELLED
     */
    public void cancel() {
        if (status != ShowtimeStatus.DRAFT && status != ShowtimeStatus.OPEN) {
            throw new IllegalStateException(
                    "Cannot cancel showtime: current status is " + status + ", expected DRAFT or OPEN");
        }
        this.status = ShowtimeStatus.CANCELLED;
    }

    /**
     * OPEN → COMPLETED (called by scheduler only)
     */
    public void complete() {
        if (status != ShowtimeStatus.OPEN) {
            throw new IllegalStateException(
                    "Cannot complete showtime: current status is " + status + ", expected OPEN");
        }
        this.status = ShowtimeStatus.COMPLETED;
    }

    /**
     * validate for booking
     */
    public void ensureBookable(Instant now) {
        if (status != ShowtimeStatus.OPEN || !startTime.isAfter(now)) {
            throw new ConflictException(
                    BusinessErrorCode.SHOWTIME_NOT_BOOKABLE,
                    "Showtime is not available for booking"
            );
        }
    }
}
