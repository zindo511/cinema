package vn.cinema.domain.booking.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.booking.entity.Booking;
import vn.cinema.domain.booking.entity.BookingStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByCustomerIdAndIdempotencyKey(Long customerId, UUID idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Booking> findAllByStatusAndExpiresAtBefore(BookingStatus status, Instant now);
}
