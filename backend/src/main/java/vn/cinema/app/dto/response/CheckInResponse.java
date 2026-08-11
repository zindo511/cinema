package vn.cinema.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import vn.cinema.domain.booking.entity.TicketStatus;
import vn.cinema.domain.cinema.entity.SeatType;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class CheckInResponse {

    // 1. Thông tin chung về trạng thái quét vé
    private String ticketCode;          // Mã vé vừa quét (VD: 550e8400-...)
    private TicketStatus status;        // Sẽ luôn là USED nếu quét thành công
    private Instant scannedAt;          // Thời điểm quét thành công
    // 2. Thông tin đối chiếu (Rất quan trọng cho nhân viên)
    private String movieTitle;          // Tên phim (VD: Avengers: Endgame)
    private String cinemaName;          // Tên cụm rạp (VD: CGV Vincom Bà Triệu)
    private String auditoriumName;      // Tên phòng chiếu (VD: Cinema 1)
    private Instant showtimeStartTime;  // Giờ bắt đầu chiếu (Để kiểm tra khách có đi nhầm giờ/nhầm ngày không)

    // 3. Vị trí chính xác của khách
    private String seatLabel;           // Số ghế (VD: H12)
    private SeatType seatType;          // Loại ghế (VD: VIP, STANDARD)
}
