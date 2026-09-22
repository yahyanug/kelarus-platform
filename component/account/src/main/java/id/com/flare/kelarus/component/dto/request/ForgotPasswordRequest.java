package id.com.flare.kelarus.component.dto.request;

import jakarta.validation.constraints.*;
import id.com.flare.kelarus.component.validation.AuthInput;

public record ForgotPasswordRequest(@NotBlank @Email @Size(max = 254) String email) {
	public ForgotPasswordRequest {
		email = AuthInput.normalizeEmail(email);
	}

	@Override
	public String toString() {
		return "ForgotPasswordRequest[REDACTED]";
	}
}
