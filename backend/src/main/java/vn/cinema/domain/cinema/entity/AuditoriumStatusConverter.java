package vn.cinema.domain.cinema.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AuditoriumStatusConverter implements AttributeConverter<AuditoriumStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(AuditoriumStatus attribute) {
        return attribute == null ? null : (short) attribute.getValue();
    }

    @Override
    public AuditoriumStatus convertToEntityAttribute(Short dbData) {
        return dbData == null ? null : AuditoriumStatus.fromValue(dbData.intValue());
    }
}
