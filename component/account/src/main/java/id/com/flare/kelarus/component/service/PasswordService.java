package id.com.flare.kelarus.component.service;

import id.com.flare.kelarus.component.dto.request.*;
import java.util.UUID;
import jakarta.validation.Valid;

public interface PasswordService {
    void forgotPassword(@Valid ForgotPasswordRequest request);
    void resetPassword(@Valid ResetPasswordRequest request);
    void changePassword(UUID authenticatedUserId, @Valid ChangePasswordRequest request);
}
