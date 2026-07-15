package vn.cinema.domain.cinema.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ScreenTypeConverter implements AttributeConverter<ScreenType, Short> {

    @Override
    public Short convertToDatabaseColumn(ScreenType attribute) {
        if (attribute == null) {
            return null;
        }
        return (short) attribute.getValue();
    }

    @Override
    public ScreenType convertToEntityAttribute(Short dbData) {
        if (dbData == null) {
            return null;
        }
        return ScreenType.fromValue(dbData.intValue());
    }
}
