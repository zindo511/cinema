package vn.cinema.domain.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import vn.cinema.domain.user.entity.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_code", nullable = false, unique = true, length = 36)
    @Builder.Default
    private String ticketCode = UUID.randomUUID().toString();

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_seat_id", nullable = false, unique = true)
    private BookingSeat bookingSeat;

    @Setter
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TicketStatus status = TicketStatus.ISSUED;

    @Setter
    @Column(name = "scanned_at")
    private Instant scannedAt;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scanned_by")
    private User scannedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void markAsUsed(User staffUser) {
        this.status = TicketStatus.USED;
        this.scannedAt = Instant.now();
        this.scannedBy = staffUser;
    }

    public void ensureNotUsed() {
        if (status == TicketStatus.USED)
            throw new IllegalStateException("Ticket status is already used");
    }
}
