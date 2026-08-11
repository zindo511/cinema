package vn.cinema.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.cinema.app.service.ShowtimeService;

/**
 * Scheduler that automatically marks OPEN showtimes as COMPLETED
 * once their cleanup_until time has passed.
 *
 * Runs every minute. The actual state transition logic and
 * transaction boundary are in ShowtimeService.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShowtimeCompletionScheduler {

    private final ShowtimeService showtimeService;

    @Scheduled(fixedRate = 60_000) // every 60 seconds
    public void completeFinishedShowtimes() {
        int count = showtimeService.completeFinishedShowtimes();
        if (count > 0) {
            log.info("Auto-completed {} showtime(s)", count);
        }
    }
}
