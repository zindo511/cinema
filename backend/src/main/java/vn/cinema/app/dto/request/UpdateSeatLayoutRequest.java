package vn.cinema.app.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import vn.cinema.domain.cinema.entity.SeatType;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSeatLayoutRequest {

    @NotNull
    @Positive
    private Integer totalRows;

    @NotNull
    @Positive
    private Integer totalColumns;

    /**
     * Optional list of seat overrides.
     * If empty/null, all seats default to STANDARD.
     * Each entry specifies a row label + seat number + seat type (e.g., VIP or COUPLE).
     */
    @Valid
    private List<SeatOverride> seatOverrides;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatOverride {

        @NotNull
        private String seatRow;

        @NotNull
        @Positive
        private Integer seatNumber;

        @NotNull
        private SeatType seatType;
    }
}
