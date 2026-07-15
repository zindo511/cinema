package vn.cinema.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.cinema.app.dto.ShowtimeResponse;
import vn.cinema.app.dto.response.ShowtimeDetailResponse;
import vn.cinema.app.mapper.ShowtimeMapper;
import vn.cinema.domain.cinema.entity.Auditorium;
import vn.cinema.domain.cinema.repository.AuditoriumRepository;
import vn.cinema.domain.cinema.repository.SeatRepository;
import vn.cinema.domain.movie.repository.MovieRepository;
import vn.cinema.domain.showtime.entity.Showtime;
import vn.cinema.domain.showtime.entity.ShowtimeSeatStatus;
import vn.cinema.domain.showtime.entity.ShowtimeStatus;
import vn.cinema.domain.showtime.port.ShowtimeRefundPort;
import vn.cinema.domain.showtime.repository.ShowtimeRepository;
import vn.cinema.domain.showtime.repository.ShowtimeSeatRepository;
import vn.cinema.domain.showtime.repository.ShowtimeSummary;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowtimeServiceTests {

    private static final ZoneId CINEMA_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Instant NOW = Instant.parse("2026-07-13T03:00:00Z");

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private ShowtimeSeatRepository showtimeSeatRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private AuditoriumRepository auditoriumRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ShowtimeRefundPort showtimeRefundPort;

    @Mock
    private ShowtimeMapper showtimeMapper;

    private ShowtimeService showtimeService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, CINEMA_ZONE);
        showtimeService = new ShowtimeService(
                showtimeRepository,
                showtimeSeatRepository,
                movieRepository,
                auditoriumRepository,
                seatRepository,
                showtimeRefundPort,
                showtimeMapper,
                clock
        );
    }

    @Test
    void usesNowAsLowerBoundWhenRequestedDateIsToday() {
        LocalDate date = LocalDate.of(2026, 7, 13);
        Instant dayEnd = Instant.parse("2026-07-13T17:00:00Z");
        ShowtimeSummary summary = mock(ShowtimeSummary.class);
        ShowtimeResponse response = ShowtimeResponse.builder()
                .id(11L)
                .startTime(Instant.parse("2026-07-13T05:00:00Z"))
                .auditoriumName("Room 1")
                .basePrice(new BigDecimal("90000"))
                .availableSeats(42L)
                .build();

        when(showtimeRepository.findAvailableShowtimes(
                1L, 2L, NOW, dayEnd, ShowtimeStatus.OPEN, ShowtimeSeatStatus.AVAILABLE
        )).thenReturn(List.of(summary));
        when(showtimeMapper.toResponse(summary)).thenReturn(response);

        List<ShowtimeResponse> result = showtimeService.getAvailableShowtimes(1L, 2L, date);

        assertThat(result).singleElement().satisfies(showtime -> {
            assertThat(showtime.getId()).isEqualTo(11L);
            assertThat(showtime.getStartTime()).isEqualTo(Instant.parse("2026-07-13T05:00:00Z"));
            assertThat(showtime.getAuditoriumName()).isEqualTo("Room 1");
            assertThat(showtime.getBasePrice()).isEqualByComparingTo("90000");
            assertThat(showtime.getAvailableSeats()).isEqualTo(42L);
        });
    }

    @Test
    void usesStartOfDayAsLowerBoundForFutureDate() {
        LocalDate date = LocalDate.of(2026, 7, 14);
        Instant dayStart = Instant.parse("2026-07-13T17:00:00Z");
        Instant dayEnd = Instant.parse("2026-07-14T17:00:00Z");

        when(showtimeRepository.findAvailableShowtimes(
                1L, 2L, dayStart, dayEnd, ShowtimeStatus.OPEN, ShowtimeSeatStatus.AVAILABLE
        )).thenReturn(List.of());

        assertThat(showtimeService.getAvailableShowtimes(1L, 2L, date)).isEmpty();
    }

    @Test
    void returnsEmptyWithoutQueryingRepositoryForPastDate() {
        List<ShowtimeResponse> result = showtimeService.getAvailableShowtimes(
                1L,
                2L,
                LocalDate.of(2026, 7, 12)
        );

        assertThat(result).isEmpty();
        verifyNoInteractions(showtimeRepository);
    }

    @Test
    void approveShowtimeChecksOpenOverlapBeforeOpening() {
        Showtime showtime = draftShowtime();
        ShowtimeDetailResponse response = new ShowtimeDetailResponse();

        when(showtimeRepository.findById(11L)).thenReturn(Optional.of(showtime));
        when(showtimeRepository.existsOpenOverlap(
                7L,
                11L,
                Instant.parse("2026-07-13T04:45:00Z"),
                Instant.parse("2026-07-13T07:15:00Z"),
                ShowtimeStatus.OPEN
        )).thenReturn(false);
        when(showtimeSeatRepository.countByShowtimeId(11L)).thenReturn(128L);
        when(showtimeMapper.toDetailResponse(showtime)).thenReturn(response);

        ShowtimeDetailResponse result = showtimeService.approveShowtime(11L);

        assertThat(showtime.getStatus()).isEqualTo(ShowtimeStatus.OPEN);
        assertThat(result.getCleanupUntil()).isEqualTo(Instant.parse("2026-07-13T07:15:00Z"));
        assertThat(result.getTotalSeats()).isEqualTo(128);
    }

    @Test
    void approveShowtimeRejectsOverlappingOpenShowtime() {
        Showtime showtime = draftShowtime();

        when(showtimeRepository.findById(11L)).thenReturn(Optional.of(showtime));
        when(showtimeRepository.existsOpenOverlap(
                7L,
                11L,
                Instant.parse("2026-07-13T04:45:00Z"),
                Instant.parse("2026-07-13T07:15:00Z"),
                ShowtimeStatus.OPEN
        )).thenReturn(true);

        assertThatThrownBy(() -> showtimeService.approveShowtime(11L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("overlaps");

        assertThat(showtime.getStatus()).isEqualTo(ShowtimeStatus.DRAFT);
        verify(showtimeSeatRepository, never()).countByShowtimeId(anyLong());
    }

    private Showtime draftShowtime() {
        return Showtime.builder()
                .id(11L)
                .auditorium(Auditorium.builder().id(7L).build())
                .startTime(Instant.parse("2026-07-13T05:00:00Z"))
                .endTime(Instant.parse("2026-07-13T07:00:00Z"))
                .status(ShowtimeStatus.DRAFT)
                .build();
    }
}
