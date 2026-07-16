package vn.cinema.domain.booking.entity;

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
import vn.cinema.domain.common.entity.BaseAuditEntity;
import vn.cinema.domain.common.exception.BusinessErrorCode;
import vn.cinema.domain.common.exception.BusinessRuleException;
import vn.cinema.domain.showtime.entity.Showtime;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "booking",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_booking_code", columnNames = "booking_code"),
                @UniqueConstraint(
                        name = "uk_booking_customer_idempotency",
                        columnNames = {"customer_id", "idempotency_key"}
                )
        },
        indexes = {
                @Index(name = "idx_booking_customer_created", columnList = "customer_id, created_at"),
                @Index(name = "idx_booking_showtime_id", columnList = "showtime_id")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Booking extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_code", nullable = false, length = 20)
    private String bookingCode;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @Column(name = "status", nullable = false)
    private BookingStatus status;

    @Column(name = "total_amount", nullable = false, precision = 12)
    private BigDecimal totalAmount;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "idempotency_key", nullable = false)
    private UUID idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    public void expire() {
        requireStatus(BookingStatus.PENDING, "expire");
        status = BookingStatus.EXPIRED;
    }

    public void confirm() {
        requireStatus(BookingStatus.PENDING, "confirm");
        status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        if (status != BookingStatus.PENDING && status != BookingStatus.CONFIRMED) {
            throw invalidTransition("cancel");
        }
        status = BookingStatus.CANCELLED;
    }

    private void requireStatus(BookingStatus expected, String transition) {
        if (status != expected) {
            throw invalidTransition(transition);
        }
    }

    private BusinessRuleException invalidTransition(String transition) {
        return new BusinessRuleException(
                BusinessErrorCode.INVALID_BOOKING_STATUS,
                "Cannot " + transition + " booking in status " + status
        );
    }
}
