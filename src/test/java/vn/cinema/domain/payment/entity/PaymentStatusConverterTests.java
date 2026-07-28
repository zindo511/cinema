package vn.cinema.domain.payment.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentStatusConverterTests {

    private final PaymentStatusConverter converter = new PaymentStatusConverter();

    @Test
    void mapsEveryPaymentStatusToItsStableDatabaseValue() {
        for (PaymentStatus status : PaymentStatus.values()) {
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
