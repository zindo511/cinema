package vn.cinema.app.mapper;

import org.springframework.stereotype.Component;
import vn.cinema.app.dto.response.BookingResponse;
import vn.cinema.app.dto.response.BookingSeatResponse;
import vn.cinema.domain.booking.entity.Booking;
import vn.cinema.domain.booking.entity.BookingSeat;

import java.util.List;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking, List<BookingSeat> bookingSeats) {
        List<BookingSeatResponse> seats = bookingSeats.stream()
                .map(bookingSeat -> BookingSeatResponse.builder()
                        .label(bookingSeat.getSeatLabel())
                        .price(bookingSeat.getPrice())
                        .build())
                .toList();

        return BookingResponse.builder()
                .bookingCode(booking.getBookingCode())
                .showtimeId(booking.getShowtime().getId())
                .status(booking.getStatus())
                .seats(seats)
                .totalAmount(booking.getTotalAmount())
                .expiresAt(booking.getExpiresAt())
                .build();
    }
}
