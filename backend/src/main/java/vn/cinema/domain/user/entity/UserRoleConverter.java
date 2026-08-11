package vn.cinema.domain.user.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, Short> {

    @Override
    public Short convertToDatabaseColumn(UserRole attribute) {
        if (attribute == null) {
            return null;
        }
        return (short) attribute.getValue();
    }

    @Override
    public UserRole convertToEntityAttribute(Short dbData) {
        if (dbData == null) {
            return null;
        }
        return UserRole.fromValue(dbData.intValue());
    }
}
