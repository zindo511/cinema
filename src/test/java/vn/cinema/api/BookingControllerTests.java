package vn.cinema.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.cinema.app.dto.response.BookingCreationResult;
import vn.cinema.app.dto.response.BookingResponse;
import vn.cinema.app.service.BookingService;
import vn.cinema.domain.booking.entity.BookingStatus;
import vn.cinema.domain.booking.port.CurrentCustomerPort;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTests {

    private static final UUID IDEMPOTENCY_KEY = UUID.fromString(
            "0d3b729d-aea4-47af-a3ff-b68a1de9f179"
    );

    @Mock
    private BookingService bookingService;
    @Mock
    private CurrentCustomerPort currentCustomerPort;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new BookingController(bookingService, currentCustomerPort))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsCreatedForNewBooking() throws Exception {
        BookingResponse response = BookingResponse.builder()
                .bookingCode("BK1234567890ABCDEF")
                .showtimeId(101L)
                .status(BookingStatus.PENDING)
                .build();
        when(currentCustomerPort.getCurrentCustomerId()).thenReturn(7L);
        when(bookingService.createBooking(eq(7L), eq(IDEMPOTENCY_KEY), any()))
                .thenReturn(new BookingCreationResult(response, true));

        mockMvc.perform(post("/api/v1/bookings")
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingCode").value("BK1234567890ABCDEF"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void returnsOkForIdempotentReplay() throws Exception {
        BookingResponse response = BookingResponse.builder()
                .bookingCode("BKEXISTING")
                .showtimeId(101L)
                .status(BookingStatus.PENDING)
                .build();
        when(currentCustomerPort.getCurrentCustomerId()).thenReturn(7L);
        when(bookingService.createBooking(eq(7L), eq(IDEMPOTENCY_KEY), any()))
                .thenReturn(new BookingCreationResult(response, false));

        mockMvc.perform(post("/api/v1/bookings")
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingCode").value("BKEXISTING"));
    }

    @Test
    void rejectsMissingIdempotencyKeyWithStableErrorCode() throws Exception {
        when(currentCustomerPort.getCurrentCustomerId()).thenReturn(7L);

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void rejectsDuplicateSeatIdsAtApiBoundary() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "showtimeId": 101,
                                  "showtimeSeatIds": [1001, 1001]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private String validRequest() {
        return """
                {
                  "showtimeId": 101,
                  "showtimeSeatIds": [1001, 1002]
                }
                """;
    }
}
