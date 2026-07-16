package vn.cinema.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import vn.cinema.domain.common.exception.BusinessErrorCode;
import vn.cinema.domain.common.exception.ConflictException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTests {

    @Test
    void returnsConflictWithMachineReadableErrorCode() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, Object>> response = handler.handleConflict(
                new ConflictException(BusinessErrorCode.SEAT_NOT_AVAILABLE, "Seat is no longer available")
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SEAT_NOT_AVAILABLE", response.getBody().get("code"));
        assertEquals("Seat is no longer available", response.getBody().get("message"));
    }
}
