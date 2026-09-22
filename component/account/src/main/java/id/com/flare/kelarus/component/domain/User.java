package id.com.flare.kelarus.component.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserStatus status;
    private Instant emailVerifiedAt;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    protected User() {}

    public User(String email, Instant now) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.status = UserStatus.PENDING_VERIFICATION;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void activate(Instant now) {
        this.status = UserStatus.ACTIVE;
        this.emailVerifiedAt = now;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public UserStatus getStatus() { return status; }
    public Instant getEmailVerifiedAt() { return emailVerifiedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
