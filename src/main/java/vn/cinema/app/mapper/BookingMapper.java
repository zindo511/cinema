package vn.cinema.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.cinema.app.dto.response.BookingResponse;
import vn.cinema.domain.booking.entity.Booking;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "seats", ignore = true)
    BookingResponse toBookingResponse(Booking booking);
}
