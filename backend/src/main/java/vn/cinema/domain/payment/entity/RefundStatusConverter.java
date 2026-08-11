package vn.cinema.domain.payment.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RefundStatusConverter implements AttributeConverter<RefundStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(RefundStatus attribute) {
        return attribute == null ? null : (short) attribute.getValue();
    }

    @Override
    public RefundStatus convertToEntityAttribute(Short dbData) {
        return dbData == null ? null : RefundStatus.fromValue(dbData.intValue());
    }
}
