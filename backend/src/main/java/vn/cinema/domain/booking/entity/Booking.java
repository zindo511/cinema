package vn.cinema.domain.booking.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.cinema.domain.common.entity.BaseAuditEntity;
import vn.cinema.domain.common.exception.BookingExpirationNotReachedException;
import vn.cinema.domain.common.exception.BookingExpiredException;
import vn.cinema.domain.common.exception.InvalidBookingStatusException;
import vn.cinema.domain.showtime.entity.Showtime;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

    @Builder.Default
    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingSeat> bookingSeats = new ArrayList<>();

    public void expire(Instant now) {
        requireStatus(BookingStatus.PENDING, "expire");
        if (expiresAt.isAfter(now)) {
            throw new BookingExpirationNotReachedException(expiresAt);
        }
        status = BookingStatus.EXPIRED;
    }

    public void confirm() {
        requireStatus(BookingStatus.PENDING, "confirm");
        status = BookingStatus.CONFIRMED;
    }

    public void ensureCancel() {
        if (status != BookingStatus.PENDING && status != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStatusException("cancel", status);
        }
    }

    public void cancel() {
        ensureCancel();
        status = BookingStatus.CANCELLED;
    }

    public void ensurePayable(Instant now) {
        requireStatus(BookingStatus.PENDING, "ensurePayable");
        if (!expiresAt.isAfter(now)) {
            throw new BookingExpiredException(expiresAt);
        }
    }

    // PRIVATE HELPER

    private void requireStatus(BookingStatus expected, String transition) {
        if (status != expected) {
            throw new InvalidBookingStatusException(transition, status);
        }
    }
}
