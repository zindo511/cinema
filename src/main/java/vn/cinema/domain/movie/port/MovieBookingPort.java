package vn.cinema.domain.movie.port;

public interface MovieBookingPort {

    boolean hasConfirmedBookings(Long movieId);
}
