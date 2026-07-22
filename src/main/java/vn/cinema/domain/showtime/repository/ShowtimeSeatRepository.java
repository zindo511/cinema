package vn.cinema.domain.showtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.showtime.entity.ShowtimeSeat;

import java.util.List;

@Repository
public interface ShowtimeSeatRepository extends JpaRepository<ShowtimeSeat, Long> {

    List<ShowtimeSeat> findByShowtimeId(Long showtimeId);

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
}
