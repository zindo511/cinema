package vn.cinema.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.payment.entity.Refund;
import vn.cinema.domain.payment.entity.RefundStatus;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findAllByStatus(RefundStatus status);
}
