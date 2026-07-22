package vn.cinema.domain.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.booking.entity.Booking;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingCode(String bookingCode);

    @EntityGraph(attributePaths = "showtime")
    Optional<Booking> findByCustomerIdAndIdempotencyKey(Long customerId, UUID idempotencyKey);
}
