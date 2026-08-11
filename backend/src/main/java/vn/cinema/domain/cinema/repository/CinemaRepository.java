package vn.cinema.domain.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.cinema.app.dto.response.CinemaDetailResponse;
import vn.cinema.domain.cinema.entity.Cinema;
import vn.cinema.domain.cinema.entity.CinemaStatus;
import vn.cinema.domain.cinema.entity.AuditoriumStatus;
import vn.cinema.domain.showtime.entity.ShowtimeStatus;

import java.time.Instant;
import java.util.List;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    @Query("SELECT DISTINCT c FROM Cinema c " +
            "JOIN Auditorium a ON a.cinema.id = c.id " +
            "JOIN Showtime s ON s.auditorium.id = a.id " +
            "WHERE s.movie.id = :movieId " +
            "AND s.startTime >= :now " +
            "AND s.status = :showtimeStatus " +
            "AND c.status = :cinemaStatus " +
            "AND a.status = :auditoriumStatus")
    List<Cinema> findCinemasCurrentlyShowingMovie(
        @Param("movieId") Long movieId,
        @Param("now") Instant now,
        @Param("showtimeStatus") ShowtimeStatus showtimeStatus,
        @Param("cinemaStatus") CinemaStatus cinemaStatus,
        @Param("auditoriumStatus") AuditoriumStatus auditoriumStatus
    );

    @Query("""
            SELECT DISTINCT city
            FROM Cinema
            ORDER BY city
           """
    )
    List<String> findDistinctCityNames();

    @Query("""
            SELECT new vn.cinema.app.dto.response.CinemaDetailResponse(c.id, c.name, c.city, c.district, c.address, c.phone, c.status)
            FROM Cinema as c
            WHERE c.city = :city AND c.status = :status
           """)
    List<CinemaDetailResponse> findDistinctByCinema(@Param("city") String city, @Param("status") CinemaStatus status);

    List<Cinema> findAllByStatus(CinemaStatus status);
}

