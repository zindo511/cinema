package vn.cinema.domain.cinema.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AuditoriumStatusConverter implements AttributeConverter<AuditoriumStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(AuditoriumStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return (short) attribute.getValue();
    }

    @Override
    public AuditoriumStatus convertToEntityAttribute(Short dbData) {
        if (dbData == null) {
            return null;
        }
        return AuditoriumStatus.fromValue(dbData.intValue());
    }
}
