package vn.cinema.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cinema.app.dto.response.CheckInResponse;
import vn.cinema.app.dto.response.MyTicketResponse;
import vn.cinema.domain.booking.entity.Ticket;
import vn.cinema.domain.booking.entity.TicketStatus;
import vn.cinema.domain.booking.repository.TicketRepository;
import vn.cinema.domain.common.exception.ResourceNotFoundException;
import vn.cinema.domain.showtime.entity.Showtime;
import vn.cinema.domain.user.entity.User;
import vn.cinema.domain.user.repository.UserRepository;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional
    public CheckInResponse handleCheckIn(String ticketCode, Long userId) {

        User staffUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        int affectedRows = ticketRepository.checkIn(
                ticketCode,
                TicketStatus.ISSUED,
                TicketStatus.USED,
                Instant.now(),
                staffUser
        );

        if (affectedRows == 0) {
            Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket code not found with: " + ticketCode));

            ticket.ensureNotUsed();
        }

        Ticket ticket = ticketRepository.findByTicketCodeWithDetails(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket code not found with: " + ticketCode));

        // từ ticket --> lấy ra thông tin phim
        Showtime showtime = ticket.getBookingSeat().getBooking().getShowtime();
        return CheckInResponse.builder()
                .ticketCode(ticketCode)
                .status(TicketStatus.USED)
                .scannedAt(ticket.getScannedAt())
                .movieTitle(showtime.getMovie().getTitle())
                .cinemaName(showtime.getAuditorium().getCinema().getName())
                .auditoriumName(showtime.getAuditorium().getName())
                .showtimeStartTime(showtime.getStartTime())
                .seatLabel(ticket.getBookingSeat().getSeatLabel())
                .seatType(ticket.getBookingSeat().getSeatType())
                .build();
    }

    @Transactional(readOnly = true)
    public List<MyTicketResponse> getMyTickets(Long customerId) {
        List<Ticket> tickets = ticketRepository.findByBookingCustomerIdOrderByCreatedAtDesc(customerId);
        return tickets.stream().map(ticket -> {
            Showtime showtime = ticket.getBookingSeat().getBooking().getShowtime();
            return MyTicketResponse.builder()
                    .ticketCode(ticket.getTicketCode())
                    .status(ticket.getStatus())
                    .movieTitle(showtime.getMovie().getTitle())
                    .posterUrl(showtime.getMovie().getPosterUrl())
                    .cinemaName(showtime.getAuditorium().getCinema().getName())
                    .auditoriumName(showtime.getAuditorium().getName())
                    .showtimeStartTime(showtime.getStartTime())
                    .seatLabel(ticket.getBookingSeat().getSeatLabel())
                    .seatType(ticket.getBookingSeat().getSeatType().name())
                    .build();
        }).toList();
    }
}
