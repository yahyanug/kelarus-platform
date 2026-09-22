package id.com.flare.kelarus.component.dto.request;

import jakarta.validation.constraints.*;

public record ChangePasswordRequest(@NotBlank @Size(min = 8, max = 128) String currentPassword,
		@NotBlank @Size(min = 8, max = 128) String newPassword) {
	@Override
	public String toString() {
		return "ChangePasswordRequest[REDACTED]";
	}
}
