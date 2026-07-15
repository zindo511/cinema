package vn.cinema.domain.movie.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MovieStatusConverter implements AttributeConverter<MovieStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(MovieStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return (short)attribute.getValue();
    }

    @Override
    public MovieStatus convertToEntityAttribute(Short movieStatus) {
        if (movieStatus == null) {
            return null;
        }
        return MovieStatus.fromValue(movieStatus.intValue());
    }
}
