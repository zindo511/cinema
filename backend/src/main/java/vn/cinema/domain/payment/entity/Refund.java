package vn.cinema.domain.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.cinema.domain.common.entity.BaseAuditEntity;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "refund",
        indexes = {
                @Index(name = "idx_refund_payment_id", columnList = "payment_id"),
                @Index(name = "idx_refund_status", columnList = "status")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Refund extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "amount", nullable = false, precision = 12)
    private BigDecimal amount;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private RefundStatus status = RefundStatus.PENDING;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "provider_refund_id", length = 100)
    private String providerRefundId;

    @Column(name = "response_code", length = 20)
    private String responseCode;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    public void ensurePending() {
        if (status != RefundStatus.PENDING) {
            throw new IllegalStateException("Refund is not pending.");
        }
    }

    public void markSuccess(String providerRefundId, String responseCode, Instant refundedAt) {
        if (status != RefundStatus.PENDING) {
            throw new IllegalStateException("Only PENDING refunds can be marked as SUCCESS");
        }
        this.status = RefundStatus.SUCCESS;
        this.providerRefundId = providerRefundId;
        this.responseCode = responseCode;
        this.refundedAt = refundedAt;
    }

    public void markFailed(String responseCode, String failureReason) {
        if (status != RefundStatus.PENDING) {
            throw new IllegalStateException("Only PENDING refunds can be marked as FAILED");
        }
        this.status = RefundStatus.FAILED;
        this.responseCode = responseCode;
        this.failureReason = failureReason;
    }
}
