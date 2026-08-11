package vn.cinema.domain.booking.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.booking.entity.Booking;
import vn.cinema.domain.booking.entity.BookingStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByIdAndCustomerId(Long id, Long customerId);

    Optional<Booking> findByCustomerIdAndIdempotencyKey(Long customerId, UUID idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Booking> findAllByStatusAndExpiresAtBefore(BookingStatus status, Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.id = :id
           """)
    Optional<Booking> findByIdForUpdate(Long id);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.customerId = :customerId
                AND (:status IS NULL OR b.status = :status)
                AND b.createdAt BETWEEN :start AND :end
           """)
    Page<Booking> findByCustomerIdAndStatusAndCreatedAtBetween(Long customerId, BookingStatus status, Instant start, Instant end, Pageable pageable);

    Optional<Booking> findByBookingCodeAndCustomerId(String bookingCode, Long customerId);
}
