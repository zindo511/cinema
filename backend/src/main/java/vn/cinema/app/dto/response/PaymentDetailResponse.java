package vn.cinema.app.dto.response;

import vn.cinema.domain.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentDetailResponse(
        Long id,
        Long bookingId,
        String paymentReference,
        String provider,
        PaymentStatus status,
        BigDecimal amount,
        String providerTransactionNo,
        String responseCode,
        String bankCode,
        String failureReason,
        Instant createdAt,
        Instant paidAt
) {
}
