package vn.cinema.domain.showtime.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ShowtimeStatusConverter implements AttributeConverter<ShowtimeStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(ShowtimeStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return (short) attribute.getValue();
    }

    @Override
    public ShowtimeStatus convertToEntityAttribute(Short dbData) {
        if (dbData == null) {
            return null;
        }
        return ShowtimeStatus.fromValue(dbData.intValue());
    }
}
