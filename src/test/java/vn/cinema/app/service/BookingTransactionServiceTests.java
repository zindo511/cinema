package vn.cinema.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.cinema.app.dto.response.BookingCreationResult;
import vn.cinema.app.mapper.BookingMapper;
import vn.cinema.config.BookingCodeGenerator;
import vn.cinema.config.BookingProperties;
import vn.cinema.domain.booking.entity.Booking;
import vn.cinema.domain.booking.repository.BookingRepository;
import vn.cinema.domain.booking.repository.BookingSeatRepository;
import vn.cinema.domain.cinema.entity.Seat;
import vn.cinema.domain.cinema.entity.SeatType;
import vn.cinema.domain.common.exception.BusinessErrorCode;
import vn.cinema.domain.common.exception.ConflictException;
import vn.cinema.domain.common.exception.ResourceNotFoundException;
import vn.cinema.domain.showtime.entity.Showtime;
import vn.cinema.domain.showtime.entity.ShowtimeSeat;
import vn.cinema.domain.showtime.entity.ShowtimeSeatStatus;
import vn.cinema.domain.showtime.entity.ShowtimeStatus;
import vn.cinema.domain.showtime.repository.ShowtimeRepository;
import vn.cinema.domain.showtime.repository.ShowtimeSeatRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingTransactionServiceTests {

    private static final Instant NOW = Instant.parse("2026-07-20T10:00:00Z");

    @Mock
    private ShowtimeRepository showtimeRepository;
    @Mock
    private ShowtimeSeatRepository showtimeSeatRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingSeatRepository bookingSeatRepository;
    @Mock
    private BookingCodeGenerator bookingCodeGenerator;

    private BookingTransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new BookingTransactionService(
                showtimeRepository,
                showtimeSeatRepository,
                bookingRepository,
                bookingSeatRepository,
                bookingCodeGenerator,
                new BookingProperties(Duration.ofMinutes(10)),
                new BookingMapper(),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void createsPendingBookingAndHoldsEverySeatAtomically() {
        UUID key = UUID.randomUUID();
        Showtime showtime = openShowtime();
        ShowtimeSeat a1 = availableSeat(1001L, showtime, "A", 1, "75000");
        ShowtimeSeat a2 = availableSeat(1002L, showtime, "A", 2, "75000");
        when(showtimeRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(showtime));
        when(bookingRepository.findByCustomerIdAndIdempotencyKey(7L, key)).thenReturn(Optional.empty());
        when(showtimeSeatRepository.findAllByIdForUpdate(101L, List.of(1001L, 1002L)))
                .thenReturn(List.of(a1, a2));
        when(bookingCodeGenerator.generate()).thenReturn("BK1234567890ABCDEF");
        when(bookingRepository.saveAndFlush(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingSeatRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookingCreationResult result = transactionService.createNewBooking(
                7L,
                key,
                "request-hash",
                101L,
                List.of(1001L, 1002L)
        );

        assertTrue(result.created());
        assertEquals("BK1234567890ABCDEF", result.response().getBookingCode());
        assertEquals(new BigDecimal("150000"), result.response().getTotalAmount());
        assertEquals(NOW.plus(Duration.ofMinutes(10)), result.response().getExpiresAt());
        assertEquals(List.of("A1", "A2"), result.response().getSeats().stream()
                .map(seat -> seat.getLabel())
                .toList());
        assertEquals(ShowtimeSeatStatus.HELD, a1.getStatus());
        assertEquals(ShowtimeSeatStatus.HELD, a2.getStatus());
        assertEquals(NOW, a1.getHeldAt());

        InOrder lockOrder = inOrder(showtimeRepository, showtimeSeatRepository);
        lockOrder.verify(showtimeRepository).findByIdForUpdate(101L);
        lockOrder.verify(showtimeSeatRepository).findAllByIdForUpdate(101L, List.of(1001L, 1002L));
    }

    @Test
    void rejectsShowtimeThatIsNotOpenBeforeLockingSeats() {
        Showtime showtime = Showtime.builder()
                .id(101L)
                .status(ShowtimeStatus.DRAFT)
                .startTime(NOW.plusSeconds(3600))
                .build();
        UUID key = UUID.randomUUID();
        when(showtimeRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(showtime));
        when(bookingRepository.findByCustomerIdAndIdempotencyKey(7L, key)).thenReturn(Optional.empty());

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> transactionService.createNewBooking(
                        7L, key, "hash", 101L, List.of(1001L)
                )
        );

        assertEquals(BusinessErrorCode.SHOWTIME_NOT_BOOKABLE.name(), exception.getCode());
        verify(showtimeSeatRepository, never()).findAllByIdForUpdate(any(), anyList());
    }

    @Test
    void rejectsUnavailableSeatWithoutCreatingPartialBooking() {
        Showtime showtime = openShowtime();
        ShowtimeSeat a1 = availableSeat(1001L, showtime, "A", 1, "75000");
        ShowtimeSeat a2 = availableSeat(1002L, showtime, "A", 2, "75000");
        a2.setStatus(ShowtimeSeatStatus.HELD);
        UUID key = UUID.randomUUID();
        when(showtimeRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(showtime));
        when(bookingRepository.findByCustomerIdAndIdempotencyKey(7L, key)).thenReturn(Optional.empty());
        when(showtimeSeatRepository.findAllByIdForUpdate(101L, List.of(1001L, 1002L)))
                .thenReturn(List.of(a1, a2));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> transactionService.createNewBooking(
                        7L, key, "hash", 101L, List.of(1001L, 1002L)
                )
        );

        assertEquals(BusinessErrorCode.SEAT_NOT_AVAILABLE.name(), exception.getCode());
        assertEquals(ShowtimeSeatStatus.AVAILABLE, a1.getStatus());
        verify(bookingRepository, never()).saveAndFlush(any());
        verify(bookingSeatRepository, never()).saveAll(anyList());
    }

    @Test
    void rejectsMissingOrCrossShowtimeSeat() {
        Showtime showtime = openShowtime();
        UUID key = UUID.randomUUID();
        when(showtimeRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(showtime));
        when(bookingRepository.findByCustomerIdAndIdempotencyKey(7L, key)).thenReturn(Optional.empty());
        when(showtimeSeatRepository.findAllByIdForUpdate(101L, List.of(1001L, 1002L)))
                .thenReturn(List.of(availableSeat(1001L, showtime, "A", 1, "75000")));

        assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.createNewBooking(
                        7L, key, "hash", 101L, List.of(1001L, 1002L)
                )
        );
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    private Showtime openShowtime() {
        return Showtime.builder()
                .id(101L)
                .status(ShowtimeStatus.OPEN)
                .startTime(NOW.plusSeconds(3600))
                .build();
    }

    private ShowtimeSeat availableSeat(
            Long id,
            Showtime showtime,
            String row,
            int number,
            String price
    ) {
        Seat seat = Seat.builder()
                .id(id)
                .seatRow(row)
                .seatNumber(number)
                .seatType(SeatType.STANDARD)
                .isActive(true)
                .build();
        return ShowtimeSeat.builder()
                .id(id)
                .showtime(showtime)
                .seat(seat)
                .status(ShowtimeSeatStatus.AVAILABLE)
                .price(new BigDecimal(price))
                .build();
    }
}
