package vn.cinema.domain.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.cinema.domain.booking.entity.Ticket;
import vn.cinema.domain.booking.entity.TicketStatus;
import vn.cinema.domain.user.entity.User;

import java.time.Instant;
import java.util.List;
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

    @Modifying
    @Query("""
            UPDATE Ticket t
            SET t.status = :used,
                t.scannedAt = :scannedAt,
                t.scannedBy = :staff
            WHERE t.ticketCode = :ticketCode
                AND t.status = :issued
           """)
    int checkIn(
            @Param("ticketCode") String ticketCode,
            @Param("issued") TicketStatus issued,
            @Param("used") TicketStatus used,
            @Param("scannedAt") Instant scannedAt,
            @Param("staff") User staff
    );

    Optional<Ticket> findByTicketCode(String ticketCode);

    @Query("""
            SELECT t
            FROM Ticket t
            JOIN FETCH t.bookingSeat bs
            JOIN FETCH bs.booking b
            JOIN FETCH b.showtime s
            JOIN FETCH s.movie m
            JOIN FETCH s.auditorium a
            JOIN FETCH a.cinema c
            WHERE b.customerId = :customerId
            ORDER BY s.startTime DESC
           """)
    List<Ticket> findByBookingCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId);

    @Modifying
    @Query("""
            UPDATE Ticket t
            SET t.status = :cancelledStatus
            WHERE t.bookingSeat.id IN (
                SELECT bs.id FROM BookingSeat bs WHERE bs.booking.id = :bookingId
            )
           """)
    void cancelTicketByBookingId(
            @Param("bookingId") Long bookingId,
            @Param("cancelledStatus") TicketStatus cancelledStatus
    );
}
