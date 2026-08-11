package vn.cinema.app.adapter.booking;

import org.springframework.stereotype.Component;
import vn.cinema.domain.movie.port.MovieBookingPort;

@Component
public class NoBookingMovieBookingAdapter implements MovieBookingPort {

    @Override
    public boolean hasConfirmedBookings(Long movieId) {
        return false;
    }
}
