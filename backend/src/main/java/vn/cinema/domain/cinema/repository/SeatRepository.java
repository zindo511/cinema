package vn.cinema.domain.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.cinema.entity.Seat;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByAuditoriumId(Long auditoriumId);

    List<Seat> findByAuditoriumIdAndIsActiveTrue(Long auditoriumId);

    void deleteByAuditoriumId(Long auditoriumId);
}
