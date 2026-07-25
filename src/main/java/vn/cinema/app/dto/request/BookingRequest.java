package vn.cinema.app.dto.request;

import jakarta.validation.constraints.NotEmpty;
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
public class BookingRequest {

    @NotNull(message = "Showtime ID is required")
    @Positive(message = "Showtime ID must be positive")
    private Long showtimeId;

    @NotEmpty(message = "Seat IDs are required")
    @Size(max = 8, message = "A booking cannot contain more than 8 seats")
    @UniqueElements(message = "Seat IDs must not contain duplicates")
    private List<
            @NotNull(message = "Seat ID must not be null")
            @Positive(message = "Seat ID must be positive")
            Long> seatIds;
}
