package vn.cinema.domain.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.booking.entity.BookingSeat;

import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    List<BookingSeat> findAllByBookingIdOrderById(Long bookingId);
}
