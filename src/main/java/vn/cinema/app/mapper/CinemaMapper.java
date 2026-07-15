package vn.cinema.app.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.cinema.app.dto.CinemaDto;
import vn.cinema.app.dto.request.CreateCinemaRequest;
import vn.cinema.app.dto.response.AuditoriumDetailResponse;
import vn.cinema.app.dto.response.CinemaDetailResponse;
import vn.cinema.domain.cinema.entity.Auditorium;
import vn.cinema.domain.cinema.entity.Cinema;
import vn.cinema.domain.cinema.entity.Seat;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CinemaMapper {

    CinemaDto toDto(Cinema cinema);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Cinema toEntity(CreateCinemaRequest request);

    CinemaDetailResponse toDetailResponse(Cinema cinema);

    @Mapping(target = "cinemaId", source = "auditorium.cinema.id")
    @Mapping(target = "cinemaName", source = "auditorium.cinema.name")
    @Mapping(target = "seats", source = "seats")
    AuditoriumDetailResponse toAuditoriumDetailResponse(Auditorium auditorium, List<Seat> seats);

    @Mapping(target = "seatType", expression = "java(seat.getSeatType().name())")
    AuditoriumDetailResponse.SeatResponse toSeatResponse(Seat seat);
}
