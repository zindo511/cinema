package vn.cinema.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
