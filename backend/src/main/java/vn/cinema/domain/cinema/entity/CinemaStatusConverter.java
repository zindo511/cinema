package vn.cinema.domain.cinema.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CinemaStatusConverter implements AttributeConverter<CinemaStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(CinemaStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return (short) attribute.getValue();
    }

    @Override
    public CinemaStatus convertToEntityAttribute(Short dbData) {
        if (dbData == null) {
            return null;
        }
        return CinemaStatus.fromValue(dbData.intValue());
    }
}
