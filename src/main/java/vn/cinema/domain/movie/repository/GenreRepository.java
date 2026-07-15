package vn.cinema.domain.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.movie.entity.Genre;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
}
