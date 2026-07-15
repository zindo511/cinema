package vn.cinema.domain.cinema.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SeatTypeConverter implements AttributeConverter<SeatType, Short> {

    @Override
    public Short convertToDatabaseColumn(SeatType attribute) {
        if (attribute == null) {
            return null;
        }
        return (short) attribute.getValue();
    }

    @Override
    public SeatType convertToEntityAttribute(Short dbData) {
        if (dbData == null) {
            return null;
        }
        return SeatType.fromValue(dbData.intValue());
    }
}
