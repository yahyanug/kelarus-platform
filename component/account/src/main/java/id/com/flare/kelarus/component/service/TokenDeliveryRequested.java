package id.com.flare.kelarus.component.service;

import java.time.Instant;
import java.util.UUID;

/**
 * In-process integration boundary. Future delivery adapters must handle this after
 * commit, keep the raw token confidential, and provide their own durable delivery
 * guarantees. No delivery adapter is installed in this module yet.
 */
public record TokenDeliveryRequested(Purpose purpose, UUID userId, String email, String token, Instant expiresAt) {
	public enum Purpose {

		EMAIL_VERIFICATION, PASSWORD_RESET

	}

	@Override
	public String toString() {
		return "TokenDeliveryRequested[REDACTED]";
	}
}
