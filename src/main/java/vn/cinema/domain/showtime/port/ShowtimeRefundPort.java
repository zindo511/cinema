package vn.cinema.domain.showtime.port;

/**
 * Port for refunding all bookings when a showtime is cancelled.
 * Follows the port/adapter pattern — the domain defines the contract,
 * the infrastructure layer provides the implementation.
 */
public interface ShowtimeRefundPort {

    /**
     * Refund all confirmed bookings for the given showtime.
     *
     * @param showtimeId the showtime being cancelled
     */
    void refundAllBookings(Long showtimeId);
}
