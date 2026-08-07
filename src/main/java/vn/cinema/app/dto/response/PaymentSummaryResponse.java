package vn.cinema.app.dto.response;

import vn.cinema.domain.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentSummaryResponse(
        Long id,
        String paymentReference,
        String provider,
        PaymentStatus status,
        BigDecimal amount,
        Instant createdAt
) {
}
