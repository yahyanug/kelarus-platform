package id.com.flare.kelarus.component.service.impl;

import id.com.flare.kelarus.component.service.*;

import id.com.flare.kelarus.component.config.AuthProperties;
import id.com.flare.kelarus.component.domain.*;
import id.com.flare.kelarus.component.dto.request.*;
import id.com.flare.kelarus.component.repository.*;
import java.time.*;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import static id.com.flare.kelarus.component.exception.AuthError.*;

@Service
@Validated
public class PasswordServiceImpl implements PasswordService {

	private final UserRepository users;

	private final UserCredentialRepository credentials;

	private final PasswordResetTokenRepository resets;

	private final RefreshTokenRepository refreshTokens;

	private final PasswordEncoder passwords;

	private final TokenService tokens;

	private final AuthProperties properties;

	private final Clock clock;

	private final ApplicationEventPublisher events;

	public PasswordServiceImpl(UserRepository users, UserCredentialRepository credentials,
			PasswordResetTokenRepository resets, RefreshTokenRepository refreshTokens, PasswordEncoder passwords,
			TokenService tokens, AuthProperties properties, Clock clock, ApplicationEventPublisher events) {
		this.users = users;
		this.credentials = credentials;
		this.resets = resets;
		this.refreshTokens = refreshTokens;
		this.passwords = passwords;
		this.tokens = tokens;
		this.properties = properties;
		this.clock = clock;
		this.events = events;
	}

	@Transactional
	@Override
	public void forgotPassword(ForgotPasswordRequest request) {
		users.findByEmail(request.email()).filter(user -> user.getStatus() == UserStatus.ACTIVE).ifPresent(user -> {
			Instant now = clock.instant();
			Instant expiry = now.plus(properties.passwordResetTtl());
			String raw = tokens.opaqueToken();
			resets.save(new PasswordResetToken(user.getId(), tokens.hash(raw), expiry, now));
			events.publishEvent(new TokenDeliveryRequested(TokenDeliveryRequested.Purpose.PASSWORD_RESET, user.getId(),
					user.getEmail(), raw, expiry));
		});
	}

	@Transactional
	@Override
	public void resetPassword(ResetPasswordRequest request) {
		String hash = tokens.hash(request.token());
		UUID userId = resets.findUserIdByTokenHash(hash).orElseThrow(INVALID_PASSWORD_RESET_TOKEN::exception);
		User user = users.findLockedById(userId).orElseThrow(INVALID_PASSWORD_RESET_TOKEN::exception);
		PasswordResetToken token = resets.findByTokenHash(hash).orElseThrow(INVALID_PASSWORD_RESET_TOKEN::exception);
		if (token.getConsumedAt() != null || user.getStatus() != UserStatus.ACTIVE) {
			throw INVALID_PASSWORD_RESET_TOKEN.exception();
		}
		Instant now = clock.instant();
		if (!token.getExpiresAt().isAfter(now)) {
			throw EXPIRED_PASSWORD_RESET_TOKEN.exception();
		}
		UserCredential credential = credentials.findByUserId(userId)
				.orElseThrow(INVALID_PASSWORD_RESET_TOKEN::exception);
		credential.changePassword(passwords.encode(request.newPassword()), now);
		token.consume(now);
		revokeSessionsAndResets(userId, now);
	}

	@Transactional
	@Override
	public void changePassword(UUID authenticatedUserId, ChangePasswordRequest request) {
		User user = users.findLockedById(authenticatedUserId).orElseThrow(INVALID_CREDENTIALS::exception);
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw ACCOUNT_NOT_ACTIVE.exception();
		}
		UserCredential credential = credentials.findByUserId(authenticatedUserId)
				.orElseThrow(INVALID_CREDENTIALS::exception);
		if (!passwords.matches(request.currentPassword(), credential.getPasswordHash())) {
			throw CURRENT_PASSWORD_INVALID.exception();
		}
		if (passwords.matches(request.newPassword(), credential.getPasswordHash())) {
			throw NEW_PASSWORD_SAME_AS_CURRENT.exception();
		}
		Instant now = clock.instant();
		credential.changePassword(passwords.encode(request.newPassword()), now);
		revokeSessionsAndResets(authenticatedUserId, now);
	}

	private void revokeSessionsAndResets(UUID userId, Instant now) {
		refreshTokens.revokeAllByUserId(userId, now);
		// A previously issued reset link must not undo a later password change.
		resets.consumeAllByUserId(userId, now);
	}

}
