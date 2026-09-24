package id.com.flare.kelarus.component.controller;

import id.com.flare.kelarus.component.dto.request.*;
import id.com.flare.kelarus.component.dto.response.GenericMessageResponse;
import id.com.flare.kelarus.component.dto.response.RegisterResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "02-Account")
@Slf4j
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@Validated
public class AccountController {

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
}
