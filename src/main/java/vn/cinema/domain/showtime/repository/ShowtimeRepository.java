package vn.cinema.domain.showtime.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.showtime.entity.Showtime;
import vn.cinema.domain.showtime.entity.ShowtimeSeatStatus;
import vn.cinema.domain.showtime.entity.ShowtimeStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT showtime FROM Showtime showtime WHERE showtime.id = :showtimeId")
    Optional<Showtime> findByIdForUpdate(@Param("showtimeId") Long showtimeId);

    @Query("""
            SELECT s.id AS id,
                   s.startTime AS startTime,
                   s.auditorium.name AS auditoriumName,
                   s.basePrice AS basePrice,
                   COUNT(ss.id) AS availableSeats
            FROM ShowtimeSeat ss
            JOIN ss.showtime s
            JOIN ss.seat seat
            WHERE ss.status = :seatStatus
              AND seat.isActive = true
              AND s.movie.id = :movieId
              AND s.auditorium.cinema.id = :cinemaId
              AND s.startTime >= :fromTime
              AND s.startTime < :dayEnd
              AND s.status = :showtimeStatus
            GROUP BY s.id, s.startTime, s.auditorium.name, s.basePrice
            ORDER BY s.startTime
            """)
    List<ShowtimeSummary> findAvailableShowtimes(
            @Param("movieId") Long movieId,
            @Param("cinemaId") Long cinemaId,
            @Param("fromTime") Instant fromTime,
            @Param("dayEnd") Instant dayEnd,
            @Param("showtimeStatus") ShowtimeStatus showtimeStatus,
            @Param("seatStatus") ShowtimeSeatStatus seatStatus
    );

    /**
     * Check whether a draft showtime overlaps an OPEN showtime in the same auditorium.
     * The cleanup buffer is pre-applied by the caller to avoid DB-side date arithmetic.
     */
    @Query("""
            SELECT COUNT(s) > 0
            FROM Showtime s
            WHERE s.auditorium.id = :auditoriumId
              AND s.id <> :showtimeId
              AND s.status = :openStatus
              AND s.endTime > :adjustedNewStart
              AND s.startTime < :newCleanupUntil
            """)
    boolean existsOpenOverlap(
            @Param("auditoriumId") Long auditoriumId,
            @Param("showtimeId") Long showtimeId,
            @Param("adjustedNewStart") Instant adjustedNewStart,
            @Param("newCleanupUntil") Instant newCleanupUntil,
            @Param("openStatus") ShowtimeStatus openStatus
    );

    /**
     * Find OPEN showtimes whose cleanup window has passed — ready for auto-completion.
     * <p>
     * cleanup_until < now  ⟺  end_time + 15min < now  ⟺  end_time < now - 15min
     * Caller passes cutoff = now - 15min.
     */
    List<Showtime> findByStatusAndEndTimeBefore(ShowtimeStatus status, Instant cutoff);
}
