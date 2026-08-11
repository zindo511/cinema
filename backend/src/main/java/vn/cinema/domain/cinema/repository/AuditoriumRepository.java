package vn.cinema.domain.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.cinema.entity.Auditorium;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditoriumRepository extends JpaRepository<Auditorium, Long> {

    List<Auditorium> findByCinemaId(Long cinemaId);

    Optional<Auditorium> findByIdAndCinemaId(Long id, Long cinemaId);

    boolean existsByCinemaIdAndName(Long cinemaId, String name);
}
