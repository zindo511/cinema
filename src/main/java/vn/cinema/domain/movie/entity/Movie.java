package vn.cinema.domain.movie.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.cinema.domain.common.entity.BaseAuditEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movie")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "status", nullable = false)
    private MovieStatus status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "movie_genre",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    public void startShowing() {
        ensureNotDeleted();
        if (status != MovieStatus.COMING_SOON) {
            throw new IllegalStateException("Movie can only move from COMING_SOON to NOW_SHOWING");
        }
        status = MovieStatus.NOW_SHOWING;
    }

    public void endShowing() {
        ensureNotDeleted();
        if (status != MovieStatus.NOW_SHOWING && status != MovieStatus.COMING_SOON) {
            throw new IllegalStateException("Movie can only move to ENDED from COMING_SOON or NOW_SHOWING");
        }
        status = MovieStatus.ENDED;
    }

    public void changeStatus(MovieStatus nextStatus) {
        if (nextStatus == null) {
            throw new IllegalArgumentException("Movie status is required");
        }
        if (nextStatus == MovieStatus.DELETED) {
            softDelete();
            return;
        }
        if (nextStatus == status) {
            return;
        }
        switch (nextStatus) {
            case NOW_SHOWING -> startShowing();
            case ENDED -> endShowing();
            case COMING_SOON -> throw new IllegalStateException("Movie cannot move back to COMING_SOON");
            case DELETED -> throw new IllegalStateException("Movie is already deleted");
        }
    }

    public void softDelete() {
        if (status == MovieStatus.DELETED) {
            throw new IllegalStateException("Movie is already deleted");
        }
        status = MovieStatus.DELETED;
    }

    private void ensureNotDeleted() {
        if (status == MovieStatus.DELETED) {
            throw new IllegalStateException("Deleted movie cannot change status");
        }
    }
}
