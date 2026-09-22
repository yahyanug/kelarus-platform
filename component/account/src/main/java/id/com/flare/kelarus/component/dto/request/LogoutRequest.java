package id.com.flare.kelarus.component.dto.request;

import jakarta.validation.constraints.*;

public record LogoutRequest(@NotBlank @Size(max = 128) String refreshToken) {
	@Override
	public String toString() {
		return "LogoutRequest[REDACTED]";
	}
}
