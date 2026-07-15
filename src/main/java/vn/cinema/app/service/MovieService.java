package vn.cinema.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cinema.app.dto.CreateMovieRequest;
import vn.cinema.app.dto.MovieDetailResponse;
import vn.cinema.app.dto.MovieListResponse;
import vn.cinema.app.dto.UpdateMovieRequest;
import vn.cinema.app.mapper.CinemaMapper;
import vn.cinema.app.mapper.MovieMapper;
import vn.cinema.domain.cinema.entity.AuditoriumStatus;
import vn.cinema.domain.cinema.entity.Cinema;
import vn.cinema.domain.cinema.entity.CinemaStatus;
import vn.cinema.domain.cinema.repository.CinemaRepository;
import vn.cinema.domain.common.exception.ResourceNotFoundException;
import vn.cinema.domain.movie.entity.Genre;
import vn.cinema.domain.movie.entity.Movie;
import vn.cinema.domain.movie.entity.MovieStatus;
import vn.cinema.domain.movie.port.MovieBookingPort;
import vn.cinema.domain.movie.repository.GenreRepository;
import vn.cinema.domain.movie.repository.MovieRepository;
import vn.cinema.domain.showtime.entity.ShowtimeStatus;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieBookingPort movieBookingPort;
    private final CinemaRepository cinemaRepository;
    private final MovieMapper movieMapper;
    private final CinemaMapper cinemaMapper;
    private final Clock clock;

    @Transactional
    public MovieDetailResponse createMovie(CreateMovieRequest request) {
        Movie movie = movieMapper.toEntity(request);
        movie.setStatus(MovieStatus.COMING_SOON);
        movie.setGenres(resolveGenres(request.getGenreIds()));
        return movieMapper.toDetailResponse(movieRepository.save(movie));
    }

    @Transactional(readOnly = true)
    public List<MovieListResponse> getNowShowingMovies(String genre, String keyword) {
        return movieRepository.findNowShowingMovies(
                        clock.instant(),
                        MovieStatus.NOW_SHOWING,
                        ShowtimeStatus.OPEN,
                        normalize(genre),
                        normalize(keyword)
                ).stream()
                .map(movieMapper::toListResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovieDetailResponse getMovieDetails(Long movieId) {
        // 1. Fetch movie or throw error
        Movie movie = movieRepository.findByIdWithGenres(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + movieId));

        // 2. Map movie entity to response DTO (genres auto-mapped, cinemas ignored)
        MovieDetailResponse response = movieMapper.toDetailResponse(movie);

        // 3. Fetch active cinemas hosting future showtimes of this movie
        List<Cinema> cinemas = cinemaRepository.findCinemasCurrentlyShowingMovie(
                movieId,
                clock.instant(),
                ShowtimeStatus.OPEN,
                CinemaStatus.ACTIVE,
                AuditoriumStatus.ACTIVE
        );

        // 4. Map cinema entities to DTOs and set on response
        response.setCinemas(
                cinemas.stream()
                        .map(cinemaMapper::toDto)
                        .collect(Collectors.toList())
        );

        return response;
    }

    @Transactional
    public MovieDetailResponse updateMovie(Long movieId, UpdateMovieRequest request) {
        Movie movie = findMovie(movieId);
        movieMapper.updateEntity(request, movie);
        if (request.getGenreIds() != null) {
            movie.setGenres(resolveGenres(request.getGenreIds()));
        }
        return movieMapper.toDetailResponse(movie);
    }

    @Transactional
    public MovieDetailResponse changeMovieStatus(Long movieId, MovieStatus status) {
        if (status == MovieStatus.DELETED) {
            return softDeleteMovie(movieId);
        }

        Movie movie = findMovie(movieId);
        movie.changeStatus(status);
        return movieMapper.toDetailResponse(movie);
    }

    @Transactional
    public MovieDetailResponse softDeleteMovie(Long movieId) {
        Movie movie = findMovie(movieId);
        if (movieBookingPort.hasConfirmedBookings(movieId)) {
            throw new IllegalStateException("Cannot delete movie with CONFIRMED bookings");
        }
        movie.softDelete();
        return movieMapper.toDetailResponse(movie);
    }

    private Movie findMovie(Long movieId) {
        return movieRepository.findByIdWithGenres(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + movieId));
    }

    private Set<Genre> resolveGenres(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Genre> genres = genreRepository.findAllById(genreIds);
        Set<Long> foundIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        Set<Long> missingIds = genreIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toCollection(HashSet::new));
        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException("Genre not found with IDs: " + missingIds);
        }
        return new HashSet<>(genres);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
