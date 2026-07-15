package vn.cinema.domain.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.movie.entity.Movie;
import vn.cinema.domain.movie.entity.MovieStatus;
import vn.cinema.domain.showtime.entity.ShowtimeStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.genres WHERE m.id = :id")
    Optional<Movie> findByIdWithGenres(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT m
            FROM Showtime st
            JOIN st.movie m
            LEFT JOIN FETCH m.genres genres
            WHERE m.status = :movieStatus
              AND st.status = :showtimeStatus
              AND st.endTime > :now
              AND (
                  :genre IS NULL OR EXISTS (
                      SELECT 1
                      FROM Movie genreMovie
                      JOIN genreMovie.genres filterGenre
                      WHERE genreMovie = m
                        AND LOWER(filterGenre.name) = LOWER(:genre)
                  )
              )
              AND (:keyword IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY m.title
            """)
    List<Movie> findNowShowingMovies(
            @Param("now") Instant now,
            @Param("movieStatus") MovieStatus movieStatus,
            @Param("showtimeStatus") ShowtimeStatus showtimeStatus,
            @Param("genre") String genre,
            @Param("keyword") String keyword
    );
}
