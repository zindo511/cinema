package vn.cinema.domain.booking.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TicketStatusConverter implements AttributeConverter<TicketStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(TicketStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return (short) attribute.getValue();
    }

    @Override
    public TicketStatus convertToEntityAttribute(Short dbData) {
        if (dbData == null) {
            return null;
        }
        return TicketStatus.fromValue(dbData.intValue());
    }
}
