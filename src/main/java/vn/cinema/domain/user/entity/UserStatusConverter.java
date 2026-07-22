package vn.cinema.domain.user.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(UserStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return (short) attribute.getValue();
    }

    @Override
    public UserStatus convertToEntityAttribute(Short dbData) {
        if (dbData == null) {
            return null;
        }
        return UserStatus.fromValue(dbData.intValue());
    }
}
