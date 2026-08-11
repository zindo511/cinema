package vn.cinema.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.cinema.app.service.PaymentService;
import vn.cinema.domain.payment.entity.Refund;
import vn.cinema.domain.payment.entity.RefundStatus;
import vn.cinema.domain.payment.repository.RefundRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundScheduler {

    private final PaymentService paymentService;
    private final RefundRepository refundRepository;

    @Scheduled(fixedRate = 300_000)
    public void refund() {
        List<Refund> pendingRefunds = refundRepository.findAllByStatus(RefundStatus.PENDING);
        // duyệt qua từng refund và xử lý nó
        for (Refund refund : pendingRefunds) {
            try {
                paymentService.processRefund(refund.getId());
            } catch (Exception e) {
                log.error("Lỗi khi xử lý refund ID: {}", refund.getId(), e);
            }
        }
    }
}
