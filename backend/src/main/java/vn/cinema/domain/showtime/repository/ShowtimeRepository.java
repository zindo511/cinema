package vn.cinema.domain.showtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.cinema.repository.CinemaShowtimeSummary;
import vn.cinema.domain.showtime.entity.Showtime;
import vn.cinema.domain.showtime.entity.ShowtimeSeatStatus;
import vn.cinema.domain.showtime.entity.ShowtimeStatus;

import java.time.Instant;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

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

    @Query("""
            SELECT m.id AS movieId,
                   m.title AS title,
                   m.posterUrl AS posterUrl,
                   m.durationMinutes AS duration,
                   s.id AS showtimeId,
                   s.startTime AS startTime,
                   s.auditorium.name AS auditoriumName,
                   s.basePrice AS basePrice,
                   COUNT(ss.id) AS availableSeats
            FROM ShowtimeSeat ss
            JOIN ss.showtime s
            JOIN ss.seat seat
            JOIN s.movie m
            WHERE ss.status = :seatStatus
                AND seat.isActive = true
                AND s.auditorium.cinema.id = :cinemaId
                AND s.startTime >= :startOfDay
                AND s.startTime < :endOfDay
                AND s.status = :showtimeStatus
                GROUP BY m.id, m.title, m.posterUrl, m.durationMinutes, s.id, s.startTime, s.auditorium.name, s.basePrice
                ORDER BY m.title, s.startTime
           """)
    List<CinemaShowtimeSummary> findAvailableShowtimesByCinemaAndDate(
            @Param("cinemaId") Long cinemaId,
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay,
            @Param("showtimeStatus") ShowtimeStatus showtimeStatus,
            @Param("seatStatus") ShowtimeSeatStatus seatStatus
    );
}
