package vn.cinema.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cinema.app.dto.request.CreateAuditoriumRequest;
import vn.cinema.app.dto.request.CreateCinemaRequest;
import vn.cinema.app.dto.request.UpdateSeatLayoutRequest;
import vn.cinema.app.dto.response.AuditoriumDetailResponse;
import vn.cinema.app.dto.response.CinemaDetailResponse;
import vn.cinema.app.mapper.CinemaMapper;
import vn.cinema.domain.cinema.entity.*;
import vn.cinema.domain.cinema.repository.AuditoriumRepository;
import vn.cinema.domain.cinema.repository.CinemaRepository;
import vn.cinema.domain.cinema.repository.SeatRepository;
import vn.cinema.domain.common.exception.BusinessErrorCode;
import vn.cinema.domain.common.exception.BusinessRuleException;
import vn.cinema.domain.common.exception.ResourceNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final SeatRepository seatRepository;
    private final CinemaMapper cinemaMapper;

    // ==================== Cinema ====================

    @Transactional
    public CinemaDetailResponse createCinema(CreateCinemaRequest request) {
        Cinema cinema = cinemaMapper.toEntity(request);
        cinema.setStatus(CinemaStatus.ACTIVE);
        return cinemaMapper.toDetailResponse(cinemaRepository.save(cinema));
    }

    public List<String> getCityNames() {
        return cinemaRepository.findDistinctCityNames();
    }

    public List<CinemaDetailResponse> getCinemas(String city) {
        if (city == null || city.isBlank()) {
            List<Cinema> cinemas = cinemaRepository.findAllByStatus(CinemaStatus.ACTIVE);
            return cinemas.stream().map(cinemaMapper::toDetailResponse).toList();
        }
        return cinemaRepository.findDistinctByCinema(city.trim(), CinemaStatus.ACTIVE);
    }

    public CinemaDetailResponse getDetailsCinema(Long cinemaId) {
        if (cinemaId == null || cinemaId < 1) {
            throw new BusinessRuleException(BusinessErrorCode.INVALID_REQUEST, "Cinema ID must be positive");
        }
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException(BusinessErrorCode.RESOURCE_NOT_FOUND, "CinemaId not found with ID: " + cinemaId));
        return cinemaMapper.toDetailResponse(cinema);
    }

    // ==================== Auditorium ====================

    @Transactional
    public AuditoriumDetailResponse createAuditorium(CreateAuditoriumRequest request) {
        // 1. Validate cinema exists
        Cinema cinema = cinemaRepository.findById(request.getCinemaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cinema not found with ID: " + request.getCinemaId()));

        // 2. Check unique name within cinema
        if (auditoriumRepository.existsByCinemaIdAndName(request.getCinemaId(), request.getName())) {
            throw new IllegalArgumentException(
                    "Auditorium name '" + request.getName() + "' already exists in cinema ID: " + request.getCinemaId());
        }

        // 3. Create auditorium entity
        Auditorium auditorium = Auditorium.builder()
                .cinema(cinema)
                .name(request.getName())
                .screenType(request.getScreenType())
                .totalRows(request.getTotalRows())
                .totalColumns(request.getTotalColumns())
                .status(AuditoriumStatus.ACTIVE)
                .build();
        auditorium = auditoriumRepository.save(auditorium);

        // 4. Auto-generate default seat grid (all STANDARD)
        List<Seat> seats = generateDefaultSeats(auditorium, request.getTotalRows(), request.getTotalColumns());
        seats = seatRepository.saveAll(seats);

        return cinemaMapper.toAuditoriumDetailResponse(auditorium, seats);
    }

    // ==================== Seat Layout ====================

    @Transactional
    public AuditoriumDetailResponse updateSeatLayout(Long auditoriumId, UpdateSeatLayoutRequest request) {
        // 1. Find auditorium
        Auditorium auditorium = auditoriumRepository.findById(auditoriumId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Auditorium not found with ID: " + auditoriumId));

        // 2. Validate seat overrides against new grid dimensions
        validateSeatOverrides(request);

        // 3. Update auditorium grid dimensions
        auditorium.setTotalRows(request.getTotalRows());
        auditorium.setTotalColumns(request.getTotalColumns());

        // 4. Delete existing seats and regenerate
        seatRepository.deleteByAuditoriumId(auditoriumId);
        seatRepository.flush();

        // 5. Generate new seat grid
        List<Seat> seats = generateDefaultSeats(auditorium, request.getTotalRows(), request.getTotalColumns());

        // 6. Apply seat overrides (VIP, COUPLE, etc.)
        if (request.getSeatOverrides() != null && !request.getSeatOverrides().isEmpty()) {
            Map<String, UpdateSeatLayoutRequest.SeatOverride> overrideMap = request.getSeatOverrides().stream()
                    .collect(Collectors.toMap(
                            o -> o.getSeatRow() + "-" + o.getSeatNumber(),
                            o -> o
                    ));

            for (Seat seat : seats) {
                String key = seat.getSeatRow() + "-" + seat.getSeatNumber();
                UpdateSeatLayoutRequest.SeatOverride override = overrideMap.get(key);
                if (override != null) {
                    seat.setSeatType(override.getSeatType());
                }
            }
        }

        seats = seatRepository.saveAll(seats);
        return cinemaMapper.toAuditoriumDetailResponse(auditorium, seats);
    }

    // ==================== Private Helpers ====================

    private List<Seat> generateDefaultSeats(Auditorium auditorium, int totalRows, int totalColumns) {
        List<Seat> seats = new ArrayList<>();
        for (int row = 0; row < totalRows; row++) {
            String rowLabel = toRowLabel(row);
            for (int col = 1; col <= totalColumns; col++) {
                seats.add(Seat.builder()
                        .auditorium(auditorium)
                        .seatRow(rowLabel)
                        .seatNumber(col)
                        .seatType(SeatType.STANDARD)
                        .isActive(true)
                        .build());
            }
        }
        return seats;
    }

    /**
     * Converts a zero-based row index to a row label: 0→A, 1→B, ..., 25→Z, 26→AA, 27→AB
     */
    private String toRowLabel(int index) {
        StringBuilder label = new StringBuilder();
        int i = index;
        do {
            label.insert(0, (char) ('A' + (i % 26)));
            i = i / 26 - 1;
        } while (i >= 0);
        return label.toString();
    }

    private void validateSeatOverrides(UpdateSeatLayoutRequest request) {
        if (request.getSeatOverrides() == null || request.getSeatOverrides().isEmpty()) {
            return;
        }

        Set<String> validRows = new HashSet<>();
        for (int i = 0; i < request.getTotalRows(); i++) {
            validRows.add(toRowLabel(i));
        }

        for (UpdateSeatLayoutRequest.SeatOverride override : request.getSeatOverrides()) {
            if (!validRows.contains(override.getSeatRow())) {
                throw new IllegalArgumentException(
                        "Invalid seat row '" + override.getSeatRow() + "'. Valid rows: " + validRows);
            }
            if (override.getSeatNumber() > request.getTotalColumns() || override.getSeatNumber() < 1) {
                throw new IllegalArgumentException(
                        "Invalid seat number " + override.getSeatNumber() +
                                ". Must be between 1 and " + request.getTotalColumns());
            }
        }
    }
}
