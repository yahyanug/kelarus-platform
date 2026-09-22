package id.com.flare.kelarus.component.controller;

import id.com.flare.kelarus.component.dto.request.*;
import id.com.flare.kelarus.component.dto.response.*;
import id.com.flare.kelarus.component.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "01-auth")
@Slf4j
@RestController
public class AuthController {

	private final AuthenticationService authentication;

	private final EmailVerificationService verification;

	private final PasswordService passwords;

	public AuthController(AuthenticationService authentication, EmailVerificationService verification,
			PasswordService passwords) {
		this.authentication = authentication;
		this.verification = verification;
		this.passwords = passwords;
	}

	@PostMapping("/v1/public/auth/register")
	@ResponseStatus(HttpStatus.CREATED)
	public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
		return authentication.register(request);
	}

	@PostMapping("/v1/public/auth/verify-email")
	public GenericMessageResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
		verification.verify(request);
		return new GenericMessageResponse("Email verified.");
	}

	@PostMapping("/v1/public/auth/login")
	public AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
		return authentication.login(request);
	}

	@PostMapping("/v1/public/auth/refresh")
	public AuthTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return authentication.refresh(request);
	}

	@PostMapping("/v1/public/auth/forgot-password")
	public GenericMessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		passwords.forgotPassword(request);
		return new GenericMessageResponse("If the account is eligible, password reset instructions will be sent.");
	}

	@PostMapping("/v1/public/auth/reset-password")
	public GenericMessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		passwords.resetPassword(request);
		return new GenericMessageResponse("Password reset.");
	}

	@PostMapping("/v1/auth/change-password")
	public GenericMessageResponse changePassword(@AuthenticationPrincipal Jwt principal,
			@Valid @RequestBody ChangePasswordRequest request) {
		passwords.changePassword(UUID.fromString(principal.getSubject()), request);
		return new GenericMessageResponse("Password changed.");
	}

	@PostMapping("/v1/auth/logout")
	public GenericMessageResponse logout(@AuthenticationPrincipal Jwt principal,
			@Valid @RequestBody LogoutRequest request) {
		authentication.logout(UUID.fromString(principal.getSubject()), request);
		return new GenericMessageResponse("Logged out.");
	}

}
