package vn.cinema.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.cinema.domain.common.entity.BaseAuditEntity;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "status", nullable = false)
    private UserStatus status;

    // ── Domain behaviour ────────────────────────────────────

    public void lock() {
        ensureNotDeleted();
        if (status == UserStatus.LOCKED) {
            throw new IllegalStateException("User is already locked");
        }
        status = UserStatus.LOCKED;
    }

    public void unlock() {
        ensureNotDeleted();
        if (status != UserStatus.LOCKED) {
            throw new IllegalStateException("Only a locked user can be unlocked");
        }
        status = UserStatus.ACTIVE;
    }

    public void softDelete() {
        if (status == UserStatus.DELETED) {
            throw new IllegalStateException("User is already deleted");
        }
        status = UserStatus.DELETED;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    private void ensureNotDeleted() {
        if (status == UserStatus.DELETED) {
            throw new IllegalStateException("Deleted user cannot change status");
        }
    }
}
