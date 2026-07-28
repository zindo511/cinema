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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.cinema.domain.booking.entity.Booking;
import vn.cinema.domain.common.entity.BaseAuditEntity;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_reference",
                        columnNames = "payment_reference"
                )
        },
        indexes = {
                @Index(name = "idx_payment_booking_id", columnList = "booking_id")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Payment extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "payment_reference", nullable = false, length = 100)
    private String paymentReference;

    @Column(name = "provider", nullable = false, length = 30)
    private String provider;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "amount", nullable = false, precision = 12)
    private BigDecimal amount;

    @Column(name = "provider_transaction_no", length = 100)
    private String providerTransactionNo;

    @Column(name = "response_code", length = 20)
    private String responseCode;

    @Column(name = "bank_code", length = 30)
    private String bankCode;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "paid_at")
    private Instant paidAt;

    public void markFailed(String reason) {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payments can be marked as FAILED. Current status: " + status);
        }

        status = PaymentStatus.FAILED;
        failureReason = reason;
    }
}

