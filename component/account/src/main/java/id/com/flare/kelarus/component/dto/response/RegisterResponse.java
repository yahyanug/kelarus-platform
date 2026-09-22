package id.com.flare.kelarus.component.dto.response;

import id.com.flare.kelarus.component.domain.UserStatus;
import java.util.UUID;

public record RegisterResponse(UUID userId, String email, UserStatus status, boolean verificationRequired) {
	@Override
	public String toString() {
		return "RegisterResponse[REDACTED]";
	}
}
