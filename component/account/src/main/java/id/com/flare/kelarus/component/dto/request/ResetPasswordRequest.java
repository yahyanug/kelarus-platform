package id.com.flare.kelarus.component.dto.request;

import jakarta.validation.constraints.*;

public record ResetPasswordRequest(
        @NotBlank @Size(max = 128) String token,
        @NotBlank @Size(min = 8, max = 128) String newPassword) {
    @Override
    public String toString() { return "ResetPasswordRequest[REDACTED]"; }
}
