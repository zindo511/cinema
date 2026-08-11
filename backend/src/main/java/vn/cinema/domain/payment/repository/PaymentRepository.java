package vn.cinema.domain.payment.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.payment.entity.Payment;
import vn.cinema.domain.payment.entity.PaymentStatus;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findFirstByBookingIdAndStatusOrderByCreatedAtDesc(
            Long bookingId,
            PaymentStatus paymentStatus
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p
            FROM Payment p
            WHERE p.paymentReference = :paymentReference
           """)
    Optional<Payment> findByPaymentReferenceForUpdate(
            @Param("paymentReference") String paymentReference
    );

    @EntityGraph(attributePaths = { "booking" })
    Optional<Payment> findByPaymentReference(String paymentReference);

    @Query("""
            SELECT p
            FROM Payment p
            JOIN p.booking b
            WHERE b.customerId = :userId
           """)
    Page<Payment> findAllByPaymentUserId(Long userId, Pageable pageable);

    @Query("""
            SELECT p
            FROM Payment p
            JOIN FETCH p.booking b
            WHERE b.customerId = :userId AND p.id = :paymentId
           """)
    Optional<Payment> findByPaymentUserId(Long userId, Long paymentId);

    Optional<Payment> findByBooking_IdAndStatus(Long bookingId, PaymentStatus status);
}
