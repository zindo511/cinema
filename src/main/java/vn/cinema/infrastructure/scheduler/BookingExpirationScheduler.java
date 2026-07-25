package vn.cinema.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.cinema.app.service.BookingService;

/**
 * <p>Scheduler tự động xử lý các booking PENDING đã hết hạn giữ ghế. </p> <br>
 * <ol>
 *     Chạy mỗi 30 giây:
 *     <li>Tìm các booking PENDING có expiresAt <= now</li>
 *     <li>Chuyển trạng thái booking sang EXPIRED</li>
 *     <li>Trả ghế từ HELD → AVAILABLE</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpirationScheduler {

    private final BookingService bookingService;

    @Scheduled(fixedRate = 30_000)
    public void releaseExpiredBookings() {
        int count = bookingService.expireBookingsAndReleaseSeats();
        if (count > 0) {
            log.info("Expired {} booking(s) and released their seats", count);
        }
    }
}
