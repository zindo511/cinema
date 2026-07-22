package vn.cinema.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import vn.cinema.app.dto.request.CreateBookingRequest;
import vn.cinema.app.dto.response.BookingCreationResult;
import vn.cinema.app.dto.response.BookingResponse;
import vn.cinema.app.mapper.BookingMapper;
import vn.cinema.domain.booking.entity.Booking;
import vn.cinema.domain.booking.repository.BookingRepository;
import vn.cinema.domain.booking.repository.BookingSeatRepository;
import vn.cinema.domain.common.exception.BusinessErrorCode;
import vn.cinema.domain.common.exception.BusinessRuleException;
import vn.cinema.domain.common.exception.ConflictException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTests {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingSeatRepository bookingSeatRepository;
    @Mock
    private BookingTransactionService bookingTransactionService;
    @Mock
    private BookingMapper bookingMapper;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                bookingRepository,
                bookingSeatRepository,
                bookingTransactionService,
                bookingMapper
        );
    }

    @Test
    void normalizesSeatOrderBeforeStartingWriteTransaction() {
        UUID key = UUID.randomUUID();
        CreateBookingRequest request = new CreateBookingRequest(101L, List.of(1002L, 1001L));
        BookingCreationResult expected = new BookingCreationResult(BookingResponse.builder().build(), true);
        when(bookingRepository.findByCustomerIdAndIdempotencyKey(7L, key)).thenReturn(Optional.empty());
        when(bookingTransactionService.createNewBooking(
                eq(7L), eq(key), anyString(), eq(101L), anyList()
        )).thenReturn(expected);

        BookingCreationResult actual = bookingService.createBooking(7L, key, request);

        assertSame(expected, actual);
        ArgumentCaptor<List<Long>> seatIds = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> requestHash = ArgumentCaptor.forClass(String.class);
        verify(bookingTransactionService).createNewBooking(
                eq(7L), eq(key), requestHash.capture(), eq(101L), seatIds.capture()
        );
        assertEquals(List.of(1001L, 1002L), seatIds.getValue());
        assertEquals(64, requestHash.getValue().length());
    }

    @Test
    void replaysExistingBookingForSameKeyAndPayload() throws Exception {
        UUID key = UUID.randomUUID();
        String hash = hash("101:1001,1002");
        Booking existing = Booking.builder().id(9L).requestHash(hash).build();
        BookingResponse response = BookingResponse.builder().bookingCode("BKEXISTING").build();
        when(bookingRepository.findByCustomerIdAndIdempotencyKey(7L, key))
                .thenReturn(Optional.of(existing));
        when(bookingSeatRepository.findAllByBookingIdOrderById(9L)).thenReturn(List.of());
        when(bookingMapper.toResponse(existing, List.of())).thenReturn(response);

        BookingCreationResult result = bookingService.createBooking(
                7L,
                key,
                new CreateBookingRequest(101L, List.of(1002L, 1001L))
        );

        assertFalse(result.created());
        assertSame(response, result.response());
        verify(bookingTransactionService, never()).createNewBooking(
                any(), any(), anyString(), any(), anyList()
        );
    }

    @Test
    void rejectsReusedKeyWithDifferentPayload() throws Exception {
        UUID key = UUID.randomUUID();
        Booking existing = Booking.builder()
                .requestHash(hash("101:1001"))
                .build();
        when(bookingRepository.findByCustomerIdAndIdempotencyKey(7L, key))
                .thenReturn(Optional.of(existing));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> bookingService.createBooking(
                        7L,
                        key,
                        new CreateBookingRequest(101L, List.of(1002L))
                )
        );

        assertEquals(BusinessErrorCode.IDEMPOTENCY_KEY_REUSED.name(), exception.getCode());
    }

    @Test
    void resolvesConcurrentIdempotencyInsertAfterWriteTransactionRollsBack() throws Exception {
        UUID key = UUID.randomUUID();
        String hash = hash("101:1001");
        Booking existing = Booking.builder().id(9L).requestHash(hash).build();
        BookingResponse response = BookingResponse.builder().bookingCode("BKWINNER").build();
        when(bookingRepository.findByCustomerIdAndIdempotencyKey(7L, key))
                .thenReturn(Optional.empty(), Optional.of(existing));
        when(bookingTransactionService.createNewBooking(
                eq(7L), eq(key), eq(hash), eq(101L), eq(List.of(1001L))
        )).thenThrow(new DataIntegrityViolationException("duplicate idempotency key"));
        when(bookingSeatRepository.findAllByBookingIdOrderById(9L)).thenReturn(List.of());
        when(bookingMapper.toResponse(existing, List.of())).thenReturn(response);

        BookingCreationResult result = bookingService.createBooking(
                7L,
                key,
                new CreateBookingRequest(101L, List.of(1001L))
        );

        assertFalse(result.created());
        assertEquals("BKWINNER", result.response().getBookingCode());
    }

    @Test
    void rejectsDuplicateSeatIdsEvenWhenCalledOutsideControllerValidation() {
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> bookingService.createBooking(
                        7L,
                        UUID.randomUUID(),
                        new CreateBookingRequest(101L, List.of(1001L, 1001L))
                )
        );

        assertEquals(BusinessErrorCode.INVALID_REQUEST.name(), exception.getCode());
        verify(bookingRepository, never()).findByCustomerIdAndIdempotencyKey(any(), any());
    }

    private String hash(String canonicalRequest) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256")
                        .digest(canonicalRequest.getBytes(StandardCharsets.UTF_8))
        );
    }
}
