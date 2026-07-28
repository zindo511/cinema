package vn.cinema.domain.payment.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(PaymentStatus attribute) {
        return attribute == null ? null : (short) attribute.getValue();
    }

    @Override
    public PaymentStatus convertToEntityAttribute(Short dbData) {
        return dbData == null ? null : PaymentStatus.fromValue(dbData.intValue());
    }
}

