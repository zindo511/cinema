package vn.cinema.domain.showtime.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.showtime.entity.ShowtimeSeat;
import vn.cinema.domain.showtime.entity.ShowtimeSeatStatus;

import java.time.Instant;
import java.util.List;

@Repository
public interface ShowtimeSeatRepository extends JpaRepository<ShowtimeSeat, Long> {
    @Query("""

            SELECT ss
        FROM ShowtimeSeat ss
        JOIN FETCH ss.seat seat
        WHERE ss.showtime.id = :showtimeId
                AND seat.isActive = true
        ORDER BY LENGTH(seat.seatRow), seat.seatRow, seat.seatNumber
        """)
    List<ShowtimeSeat> findAllWithSeatByShowtimeId(@Param("showtimeId") Long showtimeId);

    long countByShowtimeId(Long showtimeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT ss
            FROM ShowtimeSeat ss
            WHERE ss.showtime.id = :showtimeId AND ss.id IN :showtimeSeatIds
            ORDER BY ss.id
           """)
    List<ShowtimeSeat> findAllByIdInForUpdate(
            @Param("showtimeId") Long showtimeId,
            @Param("showtimeSeatIds") List<Long> showtimeSeatIds
    );
}