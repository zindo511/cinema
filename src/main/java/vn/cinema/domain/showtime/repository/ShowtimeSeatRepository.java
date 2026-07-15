package vn.cinema.domain.showtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.showtime.entity.ShowtimeSeat;

import java.util.List;

@Repository
public interface ShowtimeSeatRepository extends JpaRepository<ShowtimeSeat, Long> {

    List<ShowtimeSeat> findByShowtimeId(Long showtimeId);

    long countByShowtimeId(Long showtimeId);
}
