package id.com.flare.kelarus.component.dto.request;

import jakarta.validation.constraints.*;
import id.com.flare.kelarus.component.validation.AuthInput;

public record LoginRequest(@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Size(min = 8, max = 128) String password) {
	public LoginRequest {
		email = AuthInput.normalizeEmail(email);
	}

	@Override
	public String toString() {
		return "LoginRequest[REDACTED]";
	}
}
