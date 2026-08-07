package vn.cinema.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cinema.app.dto.response.CheckInResponse;
import vn.cinema.domain.booking.entity.Ticket;
import vn.cinema.domain.booking.entity.TicketStatus;
import vn.cinema.domain.booking.repository.TicketRepository;
import vn.cinema.domain.common.exception.ResourceNotFoundException;
import vn.cinema.domain.showtime.entity.Showtime;
import vn.cinema.domain.user.entity.User;
import vn.cinema.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional
    public CheckInResponse handleCheckIn(String ticketCode, Long userId) {

        User staffUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Ticket ticket = ticketRepository.findByTicketCodeWithDetails(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket code not found with: " + ticketCode));

        ticket.ensureNotUsed(); // check xem vé đã sử dụng chưa, nếu rồi thì bắn ra lỗi luôn.

        ticket.markAsUsed(staffUser);
        ticketRepository.save(ticket);

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
}
