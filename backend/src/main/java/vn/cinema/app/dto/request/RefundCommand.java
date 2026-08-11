package vn.cinema.app.dto.request;

import java.math.BigDecimal;
import java.time.Instant;

public record RefundCommand(
        String refundReference,
        String paymentReference,
        String providerTransactionNo,
        BigDecimal amount,
        Instant paymentCreatedAt,
        String createdBy
) {
}
