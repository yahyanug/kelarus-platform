package id.com.flare.kelarus.component.dto.request;

import jakarta.validation.constraints.*;
import id.com.flare.kelarus.component.validation.AuthInput;

public record RegisterRequest(@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Size(min = 8, max = 128) String password) {
	public RegisterRequest {
		email = AuthInput.normalizeEmail(email);
	}

	@Override
	public String toString() {
		return "RegisterRequest[REDACTED]";
	}
}
