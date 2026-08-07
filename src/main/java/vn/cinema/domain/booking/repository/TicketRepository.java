package vn.cinema.domain.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.booking.entity.Ticket;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("""
            SELECT t
            FROM Ticket t
            JOIN FETCH t.bookingSeat bs
            JOIN FETCH bs.booking b
            JOIN FETCH b.showtime s
            JOIN FETCH s.movie m
            JOIN FETCH s.auditorium a
            JOIN FETCH a.cinema c
            WHERE t.ticketCode = :ticketCode
           """)
    Optional<Ticket> findByTicketCodeWithDetails(@Param("ticketCode") String ticketCode);
}
