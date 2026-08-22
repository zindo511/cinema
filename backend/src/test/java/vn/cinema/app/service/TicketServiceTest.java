package vn.cinema.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.cinema.domain.booking.entity.Ticket;
import vn.cinema.domain.booking.repository.TicketRepository;
import vn.cinema.domain.user.entity.User;
import vn.cinema.domain.user.repository.UserRepository;

// 1. Kích hoạt Mockito để tự xử lý @Mock và @InjectMocks
@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    // KHAI BÁO CÁC ĐỐI TƯỢNG GIẢ

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    // KHAI BÁO ĐỐI TƯỢNG THẬT CẦN TEST
    // class nào phụ thuộc bên production code sẽ được tiêm vào

    @InjectMocks
    private TicketService ticketService;

    // KHAI BÁO CÁC BIẾN DÙNG CHUNG CHO CÁC TEST

    private User mockStaff;
    private Ticket mockTicket;
    private final String VALID_TICKET_CODE = "TICKET-12345";
    private final Long STAFF_ID = 1L;

    // CHUẨN BỊ MÔI TRƯỜNG (trước mỗi @Test)

    @BeforeEach
    void setUp() {
        // khởi tạo data giả để dùng lại, các class @Test bên dưới ko cần copy code
        mockStaff = User.builder()
                .id(STAFF_ID)
                .email("huyh6324@gmail.com")
                .build();
    }

    // CÁC KỊCH BẢN TEST (TEST CASES)

    @Test
    @DisplayName("Check-in: quét vé thành công và trả về thông tin chi tiết")
    void handleCheckIn_WhenTicketAndUserValid_ReturnsCheckInResponse() {
        // 1. ARRANGE (chuẩn bị hành vi cho đối tượng giả)

        // 2. ACT (gọi hàm thật của service)

        // 3. ASSERT (kiểm tra kết quả)
    }

    @Test
    @DisplayName("Check-in: báo lỗi khi không tìm thấy nhân viên quét vé")
    void handleCheckIn_WhenStaffNotFound_ThrowsResourceNotFoundException() {

        // Dùng assertThrows
    }

}
