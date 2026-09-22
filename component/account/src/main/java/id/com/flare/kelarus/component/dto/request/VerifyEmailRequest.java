package id.com.flare.kelarus.component.dto.request;

import jakarta.validation.constraints.*;

public record VerifyEmailRequest(@NotBlank @Size(max = 128) String token) {
	@Override
	public String toString() {
		return "VerifyEmailRequest[REDACTED]";
	}
}
