package id.com.flare.kelarus.component.service.impl;

import id.com.flare.kelarus.component.service.*;

import id.com.flare.kelarus.component.config.AuthProperties;
import id.com.flare.kelarus.component.domain.*;
import id.com.flare.kelarus.component.dto.request.VerifyEmailRequest;
import id.com.flare.kelarus.component.repository.*;
import java.time.*;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.validation.annotation.Validated;

import static id.com.flare.kelarus.component.exception.AuthError.*;

@Service
@Validated
public class EmailVerificationServiceImpl implements EmailVerificationService {

	private final EmailVerificationRepository verifications;

	private final UserRepository users;

	private final TokenService tokens;

	private final AuthProperties properties;

	private final Clock clock;

	private final ApplicationEventPublisher events;

	public EmailVerificationServiceImpl(EmailVerificationRepository verifications, UserRepository users,
			TokenService tokens, AuthProperties properties, Clock clock, ApplicationEventPublisher events) {
		this.verifications = verifications;
		this.users = users;
		this.tokens = tokens;
		this.properties = properties;
		this.clock = clock;
		this.events = events;
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void create(User user) {
		Instant now = clock.instant();
		Instant expiry = now.plus(properties.emailVerificationTtl());
		String raw = tokens.opaqueToken();
		verifications.save(new EmailVerification(user.getId(), tokens.hash(raw), expiry, now));
		events.publishEvent(new TokenDeliveryRequested(TokenDeliveryRequested.Purpose.EMAIL_VERIFICATION, user.getId(),
				user.getEmail(), raw, expiry));
	}

	@Transactional
	@Override
	public void verify(VerifyEmailRequest request) {
		String hash = tokens.hash(request.token());
		UUID userId = verifications.findUserIdByTokenHash(hash).orElseThrow(INVALID_VERIFICATION_TOKEN::exception);
		User user = users.findLockedById(userId).orElseThrow(INVALID_VERIFICATION_TOKEN::exception);
		EmailVerification token = verifications.findByTokenHash(hash)
				.orElseThrow(INVALID_VERIFICATION_TOKEN::exception);
		if (token.getConsumedAt() != null || user.getStatus() != UserStatus.PENDING_VERIFICATION) {
			throw INVALID_VERIFICATION_TOKEN.exception();
		}
		Instant now = clock.instant();
		if (!token.getExpiresAt().isAfter(now)) {
			throw EXPIRED_VERIFICATION_TOKEN.exception();
		}
		token.consume(now);
		user.activate(now);
	}

}
