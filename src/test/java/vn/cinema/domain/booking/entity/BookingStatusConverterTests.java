package vn.cinema.domain.booking.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingStatusConverterTests {

    private final BookingStatusConverter converter = new BookingStatusConverter();

    @Test
    void mapsEveryBookingStatusToItsStableDatabaseValue() {
        for (BookingStatus status : BookingStatus.values()) {
            Short databaseValue = converter.convertToDatabaseColumn(status);
            assertEquals(status, converter.convertToEntityAttribute(databaseValue));
        }
    }

    @Test
    void preservesNullValues() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void rejectsUnknownDatabaseValue() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute((short) 99));
    }
}
