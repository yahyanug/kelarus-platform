package id.com.flare.kelarus.component.exception;

import org.springframework.http.HttpStatus;

public enum AuthError {
    EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "Email is already registered."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password."),
    ACCOUNT_NOT_ACTIVE(HttpStatus.FORBIDDEN, "Account is not active."),
    INVALID_VERIFICATION_TOKEN(HttpStatus.BAD_REQUEST, "Invalid verification token."),
    EXPIRED_VERIFICATION_TOKEN(HttpStatus.BAD_REQUEST, "Verification token has expired."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid refresh token."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Refresh token has expired."),
    INVALID_PASSWORD_RESET_TOKEN(HttpStatus.BAD_REQUEST, "Invalid password reset token."),
    EXPIRED_PASSWORD_RESET_TOKEN(HttpStatus.BAD_REQUEST, "Password reset token has expired."),
    CURRENT_PASSWORD_INVALID(HttpStatus.BAD_REQUEST, "Current password is incorrect."),
    NEW_PASSWORD_SAME_AS_CURRENT(HttpStatus.BAD_REQUEST, "New password must differ from the current password.");

    private final HttpStatus status;
    private final String message;

    AuthError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() { return status; }
    public String message() { return message; }
    public AuthException exception() { return new AuthException(this); }
}
