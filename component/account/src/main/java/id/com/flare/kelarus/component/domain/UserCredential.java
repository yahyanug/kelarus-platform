package id.com.flare.kelarus.component.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_credentials")
public class UserCredential {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true)
	private UUID userId;

	@Column(nullable = false)
	private String passwordHash;

	@Column(nullable = false)
	private Instant passwordChangedAt;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected UserCredential() {
	}

	public UserCredential(UUID userId, String passwordHash, Instant now) {
		this.id = UUID.randomUUID();
		this.userId = userId;
		this.createdAt = now;
		changePassword(passwordHash, now);
	}

	public void changePassword(String passwordHash, Instant now) {
		this.passwordHash = passwordHash;
		this.passwordChangedAt = now;
		this.updatedAt = now;
	}

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public Instant getPasswordChangedAt() {
		return passwordChangedAt;
	}

}
