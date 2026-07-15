package vn.cinema.domain.showtime.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ShowtimeSeatStatusConverter implements AttributeConverter<ShowtimeSeatStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(ShowtimeSeatStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return (short) attribute.getValue();
    }

    @Override
    public ShowtimeSeatStatus convertToEntityAttribute(Short dbData) {
        if (dbData == null) {
            return null;
        }
        return ShowtimeSeatStatus.fromValue(dbData.intValue());
    }
}
