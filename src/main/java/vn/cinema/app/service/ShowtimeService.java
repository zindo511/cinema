package vn.cinema.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cinema.app.dto.request.CreateShowtimeRequest;
import vn.cinema.app.dto.response.ShowtimeDetailResponse;
import vn.cinema.app.dto.response.ShowtimeResponse;
import vn.cinema.app.dto.response.ShowtimeSeatMapResponse;
import vn.cinema.app.dto.response.ShowtimeSeatResponse;
import vn.cinema.app.mapper.ShowtimeMapper;
import vn.cinema.config.BookingProperties;
import vn.cinema.domain.cinema.entity.Auditorium;
import vn.cinema.domain.cinema.entity.Seat;
import vn.cinema.domain.cinema.repository.AuditoriumRepository;
import vn.cinema.domain.cinema.repository.SeatRepository;
import vn.cinema.domain.common.exception.BusinessErrorCode;
import vn.cinema.domain.common.exception.ConflictException;
import vn.cinema.domain.common.exception.ResourceNotFoundException;
import vn.cinema.domain.movie.entity.Movie;
import vn.cinema.domain.movie.repository.MovieRepository;
import vn.cinema.domain.showtime.entity.Showtime;
import vn.cinema.domain.showtime.entity.ShowtimeSeat;
import vn.cinema.domain.showtime.entity.ShowtimeSeatStatus;
import vn.cinema.domain.showtime.entity.ShowtimeStatus;
import vn.cinema.domain.showtime.port.ShowtimeRefundPort;
import vn.cinema.domain.showtime.repository.ShowtimeRepository;
import vn.cinema.domain.showtime.repository.ShowtimeSeatRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private static final Duration CLEANUP_BUFFER = Duration.ofMinutes(15);

    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeSeatRepository showtimeSeatRepository;
    private final MovieRepository movieRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRefundPort showtimeRefundPort;
    private final ShowtimeMapper showtimeMapper;
    private final Clock clock;
    private final BookingProperties bookingProperties;

    // ==================== UC-03: Query ====================

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getAvailableShowtimes(Long movieId, Long cinemaId, LocalDate date) {
        Instant now = clock.instant();
        Instant dayStart = date.atStartOfDay(clock.getZone()).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(clock.getZone()).toInstant();

        if (!dayEnd.isAfter(now)) {
            return List.of();
        }

        Instant fromTime = dayStart.isAfter(now) ? dayStart : now;

        return showtimeRepository.findAvailableShowtimes(
                        movieId,
                        cinemaId,
                        fromTime,
                        dayEnd,
                        ShowtimeStatus.OPEN,
                        ShowtimeSeatStatus.AVAILABLE
                ).stream()
                .map(showtimeMapper::toResponse)
                .toList();
    }

    // ==================== UC-10: Create Showtime ====================

    @Transactional
    public ShowtimeDetailResponse createShowtime(CreateShowtimeRequest request) {
        // 1. Validate movie exists
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movie not found with ID: " + request.getMovieId()));

        // 2. Validate auditorium exists
        Auditorium auditorium = auditoriumRepository.findById(request.getAuditoriumId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Auditorium not found with ID: " + request.getAuditoriumId()));

        // 3. Validate start_time is in the future
        Instant now = clock.instant();
        if (!request.getStartTime().isAfter(now)) {
            throw new IllegalArgumentException("Start time must be in the future");
        }

        // 4. Compute end_time from movie duration
        Instant endTime = request.getStartTime()
                .plus(Duration.ofMinutes(movie.getDurationMinutes()));

        // Create DRAFT without reserving the auditorium; overlap is checked when approving to OPEN.
        Showtime showtime = Showtime.builder()
                .movie(movie)
                .auditorium(auditorium)
                .startTime(request.getStartTime())
                .endTime(endTime)
                .basePrice(request.getBasePrice())
                .status(ShowtimeStatus.DRAFT)
                .build();
        showtime = showtimeRepository.save(showtime);

        // Create showtime_seat records from auditorium's active seats.
        List<Seat> activeSeats = seatRepository.findByAuditoriumIdAndIsActiveTrue(
                request.getAuditoriumId());
        int seatCount = createShowtimeSeats(showtime, activeSeats, request.getBasePrice());

        // Map and return response.
        return buildDetailResponse(showtime, seatCount);
    }

    // ==================== UC-10: Approve (DRAFT → OPEN) ====================

    @Transactional
    public ShowtimeDetailResponse approveShowtime(Long showtimeId) {
        Showtime showtime = findShowtime(showtimeId);
        validateCanOpen(showtime);
        showtime.approve();
        return buildDetailResponse(showtime);
    }

    // ==================== UC-10: Cancel (DRAFT/OPEN → CANCELLED) ====================

    @Transactional
    public ShowtimeDetailResponse cancelShowtime(Long showtimeId) {
        // Use the same first lock as booking creation so cancel and booking cannot commit concurrently.
        Showtime showtime = findShowtimeForUpdate(showtimeId);

        // DRAFT: chưa mở bán → chắc chắn không có booking
        // OPEN: có thể đã có booking → gọi refund port để adapter tự xử lý
        boolean wasOpen = showtime.getStatus() == ShowtimeStatus.OPEN;

        showtime.cancel();

        if (wasOpen) {
            showtimeRefundPort.refundAllBookings(showtimeId);
        }

        return buildDetailResponse(showtime);
    }

    // ==================== Scheduler: Auto-complete ====================

    /**
     * Called by ShowtimeCompletionScheduler.
     * Finds all OPEN showtimes whose cleanup window has passed and marks them COMPLETED.
     * <p>
     * cleanup_until < now  ⟺  end_time + 15min < now  ⟺  end_time < now - 15min
     */
    @Transactional
    public int completeFinishedShowtimes() {
        Instant cutoff = clock.instant().minus(CLEANUP_BUFFER);
        List<Showtime> finished = showtimeRepository.findByStatusAndEndTimeBefore(
                ShowtimeStatus.OPEN, cutoff);

        for (Showtime showtime : finished) {
            showtime.complete();
        }

        return finished.size();
    }

    @Transactional(readOnly = true)
    public ShowtimeSeatMapResponse viewSeatMap(Long showtimeId) {
        Showtime showtime = findShowtime(showtimeId);

        if (showtime.getStatus() != ShowtimeStatus.OPEN || !showtime.getStartTime().isAfter(clock.instant())) {
            throw new ConflictException(
                    BusinessErrorCode.SHOWTIME_NOT_BOOKABLE,
                    "Showtime is not available for booking"
            );
        }

        // Fetch showtime seats together with physical seat details in one query to avoid N+1.
        List<ShowtimeSeat> showtimeSeats = showtimeSeatRepository.findAllWithSeatByShowtimeId(showtimeId);

        // Map showtime seat snapshots to the DTOs returned by the seat map API.
        List<ShowtimeSeatResponse> seatResponses = showtimeSeats.stream()
                .map((seat) -> ShowtimeSeatResponse.builder()
                        .showtimeSeatId(seat.getId())
                        .label(seat.getSeat().getSeatRow() + seat.getSeat().getSeatNumber())
                        .type(seat.getSeat().getSeatType())
                        .price(seat.getPrice())
                        .status(seat.getStatus())
                        .build())
                .toList();

        return ShowtimeSeatMapResponse.builder()
                .showtimeId(showtimeId)
                .status(showtime.getStatus())
                .startTime(showtime.getStartTime())
                .expiresAfterSeconds(bookingProperties.holdDuration().getSeconds())
                .seats(seatResponses)
                .build();
    }

    // ==================== Private Helpers ====================

    private Showtime findShowtime(Long showtimeId) {
        return showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Showtime not found with ID: " + showtimeId));
    }

    private Showtime findShowtimeForUpdate(Long showtimeId) {
        return showtimeRepository.findByIdForUpdate(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Showtime not found with ID: " + showtimeId));
    }

    private ShowtimeDetailResponse buildDetailResponse(Showtime showtime) {
        int seatCount = Math.toIntExact(showtimeSeatRepository.countByShowtimeId(showtime.getId()));
        return buildDetailResponse(showtime, seatCount);
    }

    private ShowtimeDetailResponse buildDetailResponse(Showtime showtime, int seatCount) {
        ShowtimeDetailResponse response = showtimeMapper.toDetailResponse(showtime);
        response.setCleanupUntil(showtime.getEndTime().plus(CLEANUP_BUFFER));
        response.setTotalSeats(seatCount);
        return response;
    }

    private int createShowtimeSeats(Showtime showtime, List<Seat> seats, BigDecimal basePrice) {
        List<ShowtimeSeat> showtimeSeats = seats.stream()
                .map(seat -> ShowtimeSeat.builder()
                        .showtime(showtime)
                        .seat(seat)
                        .status(ShowtimeSeatStatus.AVAILABLE)
                        .price(basePrice.multiply(seat.getSeatType().getPriceMultiplier())
                                .setScale(0, RoundingMode.HALF_UP))
                        .build())
                .toList();

        showtimeSeatRepository.saveAll(showtimeSeats);
        return showtimeSeats.size();
    }

    private void validateCanOpen(Showtime showtime) {
        if (showtime.getStatus() != ShowtimeStatus.DRAFT) {
            throw new IllegalStateException(
                    "Cannot approve showtime: current status is " + showtime.getStatus() + ", expected DRAFT");
        }

        Instant now = clock.instant();
        if (!showtime.getStartTime().isAfter(now)) {
            throw new IllegalArgumentException("Cannot approve showtime whose start time is not in the future");
        }

        Instant adjustedNewStart = showtime.getStartTime().minus(CLEANUP_BUFFER);
        Instant newCleanupUntil = showtime.getEndTime().plus(CLEANUP_BUFFER);
        Long auditoriumId = showtime.getAuditorium().getId();

        boolean overlaps = showtimeRepository.existsOpenOverlap(
                auditoriumId,
                showtime.getId(),
                adjustedNewStart,
                newCleanupUntil,
                ShowtimeStatus.OPEN
        );
        if (overlaps) {
            throw new IllegalArgumentException(
                    "Showtime overlaps with an existing open showtime in auditorium ID: " + auditoriumId);
        }
    }
}
