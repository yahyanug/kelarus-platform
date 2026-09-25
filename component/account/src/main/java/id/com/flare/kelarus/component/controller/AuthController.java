package id.com.flare.kelarus.component.controller;

import id.com.flare.common.utilities.network.http.HttpRequestUtil;
import id.com.flare.kelarus.component.dto.request.*;
import id.com.flare.kelarus.component.dto.response.*;
import id.com.flare.kelarus.component.security.annotation.PublicAuth;
import id.com.flare.kelarus.component.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "01-authentication")
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

	private final AuthenticationService authenticationService;

	private final EmailVerificationService verification;

	private final PasswordService passwords;

	@PublicAuth
	@PostMapping("/request-otp")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<OtpResponse> requestOTP(@Valid @RequestBody OtpRequest otpRequest,
			HttpServletRequest httpServletRequest) {
		otpRequest.setHttpDetail(HttpRequestUtil.getDetails(httpServletRequest));
		return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.requestOtp(otpRequest));
	}

	@PublicAuth
	@GetMapping("/verify-otp")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void verifyOTP() {
		// authenticationService.verifyOtp();
	}

	@PublicAuth
	@PostMapping("/v1/public/auth/login")
	public AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
		// return authentication.login(request);
		return null;
	}

	@PostMapping("/v1/auth/logout")
	public GenericMessageResponse logout(@AuthenticationPrincipal Jwt principal,
			@Valid @RequestBody LogoutRequest request) {
		// authentication.logout(UUID.fromString(principal.getSubject()), request);
		return new GenericMessageResponse("Logged out.");
	}

	@PostMapping("/v1/public/auth/refresh")
	public AuthTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		// return authenticationService.refresh(request);
		return null;
	}

}
