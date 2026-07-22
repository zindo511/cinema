package vn.cinema.app.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    @NotNull(message = "Showtime ID is required")
    @Positive(message = "Showtime ID must be positive")
    private Long showtimeId;

    @NotNull(message = "Showtime seat IDs are required")
    @Size(min = 1, max = 8, message = "A booking must contain between 1 and 8 seats")
    @UniqueElements(message = "Showtime seat IDs must not contain duplicates")
    private List<@NotNull(message = "Showtime seat ID is required")
            @Positive(message = "Showtime seat ID must be positive") Long> showtimeSeatIds;
}
