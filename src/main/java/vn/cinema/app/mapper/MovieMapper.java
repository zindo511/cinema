package vn.cinema.app.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import vn.cinema.app.dto.CreateMovieRequest;
import vn.cinema.app.dto.MovieDetailResponse;
import vn.cinema.app.dto.MovieListResponse;
import vn.cinema.app.dto.UpdateMovieRequest;
import vn.cinema.domain.movie.entity.Genre;
import vn.cinema.domain.movie.entity.Movie;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "genres", ignore = true)
    Movie toEntity(CreateMovieRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(UpdateMovieRequest request, @MappingTarget Movie movie);

    @Mapping(target = "genres", source = "genres", qualifiedByName = "genresToNames")
    @Mapping(target = "cinemas", ignore = true)
    MovieDetailResponse toDetailResponse(Movie movie);

    @Mapping(target = "genres", source = "genres", qualifiedByName = "genresToNames")
    MovieListResponse toListResponse(Movie movie);

    @Named("genresToNames")
    default Set<String> genresToNames(Set<Genre> genres) {
        if (genres == null) {
            return Set.of();
        }
        return genres.stream()
                .map(Genre::getName)
                .collect(Collectors.toSet());
    }
}
