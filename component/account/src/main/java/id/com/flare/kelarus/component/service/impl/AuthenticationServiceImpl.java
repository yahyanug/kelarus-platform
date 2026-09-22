package id.com.flare.kelarus.component.service.impl;

import id.com.flare.kelarus.component.service.*;

import id.com.flare.kelarus.component.config.AuthProperties;
import id.com.flare.kelarus.component.domain.*;
import id.com.flare.kelarus.component.dto.request.*;
import id.com.flare.kelarus.component.dto.response.*;
import id.com.flare.kelarus.component.repository.*;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import static id.com.flare.kelarus.component.exception.AuthError.*;

@Service
@Validated
public class AuthenticationServiceImpl implements AuthenticationService {

	private final UserRepository users;

	private final UserCredentialRepository credentials;

	private final RefreshTokenRepository refreshTokens;

	private final EmailVerificationService verification;

	private final PasswordEncoder passwords;

	private final TokenService tokens;

	private final AuthProperties properties;

	private final Clock clock;

	private final String dummyPasswordHash;

	public AuthenticationServiceImpl(UserRepository users, UserCredentialRepository credentials,
			RefreshTokenRepository refreshTokens, EmailVerificationService verification, PasswordEncoder passwords,
			TokenService tokens, AuthProperties properties, Clock clock) {
		this.users = users;
		this.credentials = credentials;
		this.refreshTokens = refreshTokens;
		this.verification = verification;
		this.passwords = passwords;
		this.tokens = tokens;
		this.properties = properties;
		this.clock = clock;
		this.dummyPasswordHash = passwords.encode(UUID.randomUUID().toString());
	}

	@Transactional
	@Override
	public RegisterResponse register(RegisterRequest request) {
		if (users.existsByEmail(request.email())) {
			throw EMAIL_ALREADY_REGISTERED.exception();
		}
		Instant now = clock.instant();
		User user = users.saveAndFlush(new User(request.email(), now));
		credentials.save(new UserCredential(user.getId(), passwords.encode(request.password()), now));
		verification.create(user);
		return new RegisterResponse(user.getId(), user.getEmail(), user.getStatus(), true);
	}

	@Transactional
	@Override
	public AuthTokenResponse login(LoginRequest request) {
		User user = users.findByEmail(request.email()).orElse(null);
		UserCredential credential = user == null ? null : credentials.findByUserId(user.getId()).orElse(null);
		boolean matches = passwords.matches(request.password(),
				credential == null ? dummyPasswordHash : credential.getPasswordHash());
		if (user == null || credential == null || !matches) {
			throw INVALID_CREDENTIALS.exception();
		}
		requireActive(user);
		return issueTokens(user);
	}

	@Transactional
	@Override
	public AuthTokenResponse refresh(RefreshTokenRequest request) {
		String hash = tokens.hash(request.refreshToken());
		UUID userId = refreshTokens.findUserIdByTokenHash(hash).orElseThrow(INVALID_REFRESH_TOKEN::exception);
		User user = users.findLockedById(userId).orElseThrow(INVALID_REFRESH_TOKEN::exception);
		RefreshToken token = refreshTokens.findByTokenHash(hash).orElseThrow(INVALID_REFRESH_TOKEN::exception);
		if (token.getRevokedAt() != null) {
			throw INVALID_REFRESH_TOKEN.exception();
		}
		if (!token.getExpiresAt().isAfter(clock.instant())) {
			throw EXPIRED_REFRESH_TOKEN.exception();
		}
		requireActive(user);
		token.revoke(clock.instant());
		return issueTokens(user);
	}

	@Transactional
	@Override
	public void logout(UUID authenticatedUserId, LogoutRequest request) {
		users.findLockedById(authenticatedUserId).orElseThrow(INVALID_CREDENTIALS::exception);
		refreshTokens.findByTokenHash(tokens.hash(request.refreshToken()))
				.filter(token -> token.getUserId().equals(authenticatedUserId))
				.ifPresent(token -> token.revoke(clock.instant()));
	}

	private AuthTokenResponse issueTokens(User user) {
		Instant now = clock.instant();
		String raw = tokens.opaqueToken();
		refreshTokens
				.save(new RefreshToken(user.getId(), tokens.hash(raw), now.plus(properties.refreshTokenTtl()), now));
		return new AuthTokenResponse(tokens.accessToken(user), raw, "Bearer", properties.accessTokenTtl().toSeconds());
	}

	private void requireActive(User user) {
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw ACCOUNT_NOT_ACTIVE.exception();
		}
	}

}
