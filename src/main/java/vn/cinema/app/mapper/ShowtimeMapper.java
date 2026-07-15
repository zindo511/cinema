package vn.cinema.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.cinema.app.dto.ShowtimeResponse;
import vn.cinema.app.dto.response.ShowtimeDetailResponse;
import vn.cinema.domain.showtime.entity.Showtime;
import vn.cinema.domain.showtime.repository.ShowtimeSummary;

@Mapper(componentModel = "spring")
public interface ShowtimeMapper {

    ShowtimeResponse toResponse(ShowtimeSummary summary);

    @Mapping(target = "movieId", source = "movie.id")
    @Mapping(target = "movieTitle", source = "movie.title")
    @Mapping(target = "auditoriumId", source = "auditorium.id")
    @Mapping(target = "auditoriumName", source = "auditorium.name")
    @Mapping(target = "status", expression = "java(showtime.getStatus().name())")
    @Mapping(target = "totalSeats", ignore = true)
    @Mapping(target = "cleanupUntil", ignore = true)
    ShowtimeDetailResponse toDetailResponse(Showtime showtime);
}
