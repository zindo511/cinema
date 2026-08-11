package vn.cinema.domain.booking.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BookingStatusConverter implements AttributeConverter<BookingStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(BookingStatus attribute) {
        return attribute == null ? null : (short) attribute.getValue();
    }

    @Override
    public BookingStatus convertToEntityAttribute(Short dbData) {
        return dbData == null ? null : BookingStatus.fromValue(dbData.intValue());
    }
}
