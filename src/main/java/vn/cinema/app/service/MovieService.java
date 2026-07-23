package vn.cinema.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cinema.app.dto.request.CreateMovieRequest;
import vn.cinema.app.dto.request.UpdateMovieRequest;
import vn.cinema.app.dto.response.MovieDetailResponse;
import vn.cinema.app.dto.response.MovieListResponse;
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

    /**
     * Tạo mới một bộ phim trong hệ thống với trạng thái mặc định COMING_SOON
     * và ánh xạ danh sách các thể loại tương ứng.
     *
     * @param request Thông tin phim cần tạo mới
     * @return Thông tin chi tiết phim vừa được tạo
     */
    @Transactional
    public MovieDetailResponse createMovie(CreateMovieRequest request) {
        Movie movie = movieMapper.toEntity(request);
        movie.setStatus(MovieStatus.COMING_SOON);
        movie.setGenres(resolveGenres(request.getGenreIds()));
        return movieMapper.toDetailResponse(movieRepository.save(movie));
    }

    /**
     * Lấy danh sách các bộ phim đang chiếu (NOW_SHOWING) có lịch chiếu mở (OPEN)
     * tính đến thời điểm hiện tại. Hỗ trợ lọc theo thể loại và từ khóa tìm kiếm.
     *
     * @param genre Thể loại phim cần lọc (tùy chọn)
     * @param keyword Từ khóa tìm kiếm theo tên phim (tùy chọn)
     * @return Danh sách phim đang chiếu thỏa mãn điều kiện
     */
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

    /**
     * Lấy thông tin chi tiết của một bộ phim theo ID, bao gồm danh sách các thể loại
     * và danh sách các rạp chiếu đang có lịch chiếu mở cho bộ phim này trong tương lai.
     *
     * @param movieId ID của phim cần lấy thông tin
     * @return Thông tin chi tiết của bộ phim và các rạp đang chiếu
     */
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

    /**
     * Cập nhật thông tin chi tiết của bộ phim (tên, mô tả, thời lượng, poster, trailer, 
     * ngày phát hành, danh sách thể loại...).
     *
     * @param movieId ID của phim cần cập nhật
     * @param request Dữ liệu cập nhật phim
     * @return Thông tin chi tiết phim sau khi cập nhật
     */
    @Transactional
    public MovieDetailResponse updateMovie(Long movieId, UpdateMovieRequest request) {
        Movie movie = findMovie(movieId);
        movieMapper.updateEntity(request, movie);
        if (request.getGenreIds() != null) {
            movie.setGenres(resolveGenres(request.getGenreIds()));
        }
        return movieMapper.toDetailResponse(movie);
    }

    /**
     * Thay đổi trạng thái hoạt động của bộ phim (chẳng hạn COMING_SOON -> NOW_SHOWING, STOPPED, ...).
     * Nếu trạng thái mới là DELETED, chuyển hướng sang xử lý xóa mềm (soft delete).
     *
     * @param movieId ID của phim cần thay đổi trạng thái
     * @param status Trạng thái mới của phim
     * @return Thông tin chi tiết phim sau khi đổi trạng thái
     */
    @Transactional
    public MovieDetailResponse changeMovieStatus(Long movieId, MovieStatus status) {
        if (status == MovieStatus.DELETED) {
            return softDeleteMovie(movieId);
        }

        Movie movie = findMovie(movieId);
        movie.changeStatus(status);
        return movieMapper.toDetailResponse(movie);
    }

    /**
     * Thực hiện xóa mềm (soft delete) bộ phim.
     * Kiểm tra nếu bộ phim đã có vé đặt thành công (CONFIRMED bookings) thì không cho phép xóa.
     *
     * @param movieId ID của phim cần xóa mềm
     * @return Thông tin chi tiết phim sau khi xóa mềm
     */
    @Transactional
    public MovieDetailResponse softDeleteMovie(Long movieId) {
        Movie movie = findMovie(movieId);
        if (movieBookingPort.hasConfirmedBookings(movieId)) {
            throw new IllegalStateException("Cannot delete movie with CONFIRMED bookings");
        }
        movie.softDelete();
        return movieMapper.toDetailResponse(movie);
    }

    /**
     * Helper method hỗ trợ tìm kiếm entity Movie theo ID kèm danh sách thể loại,
     * ném ra ResourceNotFoundException nếu không tìm thấy.
     *
     * @param movieId ID của phim cần tìm
     * @return Entity Movie tìm được
     */
    private Movie findMovie(Long movieId) {
        return movieRepository.findByIdWithGenres(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + movieId));
    }

    /**
     * Helper method kiểm tra và ánh xạ danh sách ID thể loại (genreIds) sang danh sách entity Genre.
     * Ném ra ResourceNotFoundException nếu có bất kỳ ID thể loại nào không tồn tại.
     *
     * @param genreIds Tập hợp các ID thể loại cần tìm
     * @return Tập hợp các entity Genre tương ứng
     */
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

    /**
     * Helper method chuẩn hóa chuỗi tìm kiếm đầu vào (cắt khoảng trắng thừa),
     * trả về null nếu chuỗi rỗng hoặc chỉ chứa khoảng trắng.
     *
     * @param value Chuỗi gốc đầu vào
     * @return Chuỗi đã được chuẩn hóa hoặc null
     */
    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
