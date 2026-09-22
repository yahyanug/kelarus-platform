package id.com.flare.kelarus.component;

import id.com.flare.kelarus.component.domain.*;
import id.com.flare.kelarus.component.dto.request.*;
import id.com.flare.kelarus.component.dto.response.*;
import id.com.flare.kelarus.component.exception.*;
import id.com.flare.kelarus.component.repository.*;
import id.com.flare.kelarus.component.service.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.*;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static id.com.flare.kelarus.component.exception.AuthError.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:account;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "eureka.client.enabled=false"
})
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@Import(AuthenticationIntegrationTest.TestConfiguration.class)
class AuthenticationIntegrationTest {
    private static final String EMAIL = "person@example.test";
    private static final String PASSWORD = "initial-password";
    private static final String NEW_PASSWORD = "updated-password";
    private static final Instant NOW = Instant.parse("2026-09-22T10:00:00Z");

    @Autowired AuthenticationService auth;
    @Autowired EmailVerificationService verification;
    @Autowired PasswordService passwords;
    @Autowired TokenService tokens;
    @Autowired UserRepository users;
    @Autowired UserCredentialRepository credentials;
    @Autowired RefreshTokenRepository refreshTokens;
    @Autowired EmailVerificationRepository verifications;
    @Autowired PasswordResetTokenRepository resets;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtDecoder decoder;
    @Autowired JwtEncoder jwtEncoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @Autowired MutableClock clock;
    @Autowired DeliveryCollector delivery;

    @DynamicPropertySource
    static void securityProperties(DynamicPropertyRegistry registry) {
        byte[] key = new byte[32];
        new java.security.SecureRandom().nextBytes(key);
        registry.add("kelarus.auth.jwt-secret", () -> Base64.getEncoder().encodeToString(key));
    }

    @BeforeEach
    void cleanDatabase() {
        jdbc.update("delete from password_reset_tokens");
        jdbc.update("delete from email_verifications");
        jdbc.update("delete from refresh_tokens");
        jdbc.update("delete from user_credentials");
        jdbc.update("delete from users");
        clock.now = NOW;
        delivery.events.clear();
    }

    @Test
    void registrationNormalizesEmailHashesPasswordAndRequiresVerification() {
        RegisterResponse result = auth.register(new RegisterRequest("  PERSON@Example.Test  ", PASSWORD));
        User user = users.findById(result.userId()).orElseThrow();
        assertThat(result.email()).isEqualTo(EMAIL);
        assertThat(result.status()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(result.verificationRequired()).isTrue();
        assertThat(user.getEmailVerifiedAt()).isNull();
        UserCredential credential = credentials.findByUserId(user.getId()).orElseThrow();
        assertThat(credential.getPasswordHash()).isNotEqualTo(PASSWORD);
        assertThat(encoder.matches(PASSWORD, credential.getPasswordHash())).isTrue();
        TokenDeliveryRequested event = delivery.last();
        EmailVerification stored = verifications.findByTokenHash(tokens.hash(event.token())).orElseThrow();
        assertThat(stored.getTokenHash()).hasSize(64).isNotEqualTo(event.token());
        assertThat(stored.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofHours(24)));
        assertThat(event.toString()).doesNotContain(event.token(), EMAIL);
    }

    @Test
    void duplicateEmailIsRejectedAfterNormalization() {
        register();
        assertError(() -> auth.register(new RegisterRequest(" PERSON@EXAMPLE.TEST ", PASSWORD)), EMAIL_ALREADY_REGISTERED);
        assertThat(users.count()).isEqualTo(1);
        assertThat(credentials.count()).isEqualTo(1);
    }

    @Test
    void verificationActivatesUserAndCannotBeReused() {
        RegisterResponse result = register();
        String raw = delivery.last().token();
        verification.verify(new VerifyEmailRequest(raw));
        User user = users.findById(result.userId()).orElseThrow();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getEmailVerifiedAt()).isEqualTo(NOW);
        assertThat(verifications.findByTokenHash(tokens.hash(raw)).orElseThrow().getConsumedAt()).isEqualTo(NOW);
        assertError(() -> verification.verify(new VerifyEmailRequest(raw)), INVALID_VERIFICATION_TOKEN);
    }

    @Test
    void invalidVerificationIsRejected() {
        assertError(() -> verification.verify(new VerifyEmailRequest("missing")), INVALID_VERIFICATION_TOKEN);
    }

    @Test
    void verificationExpiresAtExactBoundary() {
        RegisterResponse result = register();
        String raw = delivery.last().token();
        clock.now = NOW.plus(Duration.ofHours(24));
        assertError(() -> verification.verify(new VerifyEmailRequest(raw)), EXPIRED_VERIFICATION_TOKEN);
        assertThat(users.findById(result.userId()).orElseThrow().getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(verifications.findByTokenHash(tokens.hash(raw)).orElseThrow().getConsumedAt()).isNull();
    }

    @Test
    void loginReturnsIdentityJwtAndOnlyPersistsRefreshHash() {
        UUID id = activate();
        AuthTokenResponse result = login();
        Jwt jwt = decoder.decode(result.accessToken());
        assertThat(jwt.getSubject()).isEqualTo(id.toString());
        assertThat(jwt.getClaimAsString("email")).isEqualTo(EMAIL);
        assertThat(jwt.getClaims().keySet()).containsExactlyInAnyOrder("sub", "email", "iat", "exp");
        assertThat(jwt.getExpiresAt()).isEqualTo(NOW.plusSeconds(900));
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresIn()).isEqualTo(900);
        RefreshToken stored = refreshTokens.findByTokenHash(tokens.hash(result.refreshToken())).orElseThrow();
        assertThat(stored.getTokenHash()).hasSize(64).isNotEqualTo(result.refreshToken());
        assertThat(stored.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofDays(30)));
    }

    @Test
    void loginUnknownEmailAndWrongPasswordUseSameError() {
        activate();
        assertError(() -> auth.login(new LoginRequest(EMAIL, "wrong-password")), INVALID_CREDENTIALS);
        assertError(() -> auth.login(new LoginRequest("missing@example.test", PASSWORD)), INVALID_CREDENTIALS);
    }

    @Test
    void pendingAccountRequiresVerificationButWrongPasswordStillUsesGenericError() {
        register();
        assertError(this::login, ACCOUNT_NOT_ACTIVE);
        assertError(() -> auth.login(new LoginRequest(EMAIL, "wrong-password")), INVALID_CREDENTIALS);
        assertThat(refreshTokens.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"LOCKED", "DISABLED"})
    void unavailableAccountsCannotLoginOrRefresh(String status) {
        UUID id = activate();
        AuthTokenResponse session = login();
        jdbc.update("update users set status = ? where id = ?", status, id);
        assertError(this::login, ACCOUNT_NOT_ACTIVE);
        assertError(() -> auth.refresh(new RefreshTokenRequest(session.refreshToken())), ACCOUNT_NOT_ACTIVE);
    }

    @Test
    void refreshRotatesTokenAndRejectsReuse() {
        activate();
        AuthTokenResponse first = login();
        AuthTokenResponse second = auth.refresh(new RefreshTokenRequest(first.refreshToken()));
        assertThat(second.refreshToken()).isNotEqualTo(first.refreshToken());
        assertThat(decoder.decode(second.accessToken()).getClaimAsString("email")).isEqualTo(EMAIL);
        assertThat(refreshTokens.findByTokenHash(tokens.hash(first.refreshToken())).orElseThrow().getRevokedAt()).isEqualTo(NOW);
        assertThat(refreshTokens.findByTokenHash(tokens.hash(second.refreshToken())).orElseThrow().getRevokedAt()).isNull();
        assertError(() -> auth.refresh(new RefreshTokenRequest(first.refreshToken())), INVALID_REFRESH_TOKEN);
    }

    @Test
    void expiredAndMissingRefreshTokensAreRejected() {
        activate();
        String raw = login().refreshToken();
        clock.now = NOW.plus(Duration.ofDays(30));
        assertError(() -> auth.refresh(new RefreshTokenRequest(raw)), EXPIRED_REFRESH_TOKEN);
        assertError(() -> auth.refresh(new RefreshTokenRequest("missing")), INVALID_REFRESH_TOKEN);
    }

    @Test
    void concurrentRefreshHasOnlyOneWinner() throws Exception {
        activate();
        String raw = login().refreshToken();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> attempt = () -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) throw new AssertionError("Start timed out");
                try {
                    auth.refresh(new RefreshTokenRequest(raw));
                    return true;
                } catch (AuthException ex) {
                    assertThat(ex.getError()).isEqualTo(INVALID_REFRESH_TOKEN);
                    return false;
                }
            };
            Future<Boolean> one = executor.submit(attempt);
            Future<Boolean> two = executor.submit(attempt);
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(List.of(one.get(20, TimeUnit.SECONDS), two.get(20, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        }
        assertThat(refreshTokens.findAll().stream().filter(t -> t.getRevokedAt() == null)).hasSize(1);
    }

    @Test
    void logoutIsIdempotentAndOnlyRevokesOwnedToken() {
        UUID owner = activate();
        String raw = login().refreshToken();
        RegisterResponse other = auth.register(new RegisterRequest("other@example.test", PASSWORD));
        verification.verify(new VerifyEmailRequest(delivery.last().token()));
        auth.logout(other.userId(), new LogoutRequest(raw));
        assertThat(refreshTokens.findByTokenHash(tokens.hash(raw)).orElseThrow().getRevokedAt()).isNull();
        auth.logout(owner, new LogoutRequest(raw));
        auth.logout(owner, new LogoutRequest(raw));
        auth.logout(owner, new LogoutRequest("missing"));
        assertError(() -> auth.refresh(new RefreshTokenRequest(raw)), INVALID_REFRESH_TOKEN);
    }

    @Test
    void forgotPasswordHasSamePublicResponseForExistingAndMissingEmail() throws Exception {
        activate();
        String existing = mvc.perform(post("/v1/public/auth/forgot-password")
                        .contentType("application/json").content("{\"email\":\" PERSON@EXAMPLE.TEST \"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String missing = mvc.perform(post("/v1/public/auth/forgot-password")
                        .contentType("application/json").content("{\"email\":\"missing@example.test\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(existing).isEqualTo(missing).doesNotContain(delivery.last().token());
        assertThat(resets.count()).isEqualTo(1);
    }

    @Test
    void forgotPasswordDoesNotIssueForPendingAccount() {
        register();
        passwords.forgotPassword(new ForgotPasswordRequest(EMAIL));
        assertThat(resets.count()).isZero();
    }

    @Test
    void resetChangesPasswordConsumesAllResetTokensAndRevokesAllSessions() {
        UUID id = activate();
        String refresh = login().refreshToken();
        login();
        String firstReset = requestReset();
        String secondReset = requestReset();
        passwords.resetPassword(new ResetPasswordRequest(firstReset, NEW_PASSWORD));
        assertThat(encoder.matches(NEW_PASSWORD, credentials.findByUserId(id).orElseThrow().getPasswordHash())).isTrue();
        assertThat(refreshTokens.findAll()).allSatisfy(t -> assertThat(t.getRevokedAt()).isEqualTo(NOW));
        assertThat(resets.findAll()).allSatisfy(t -> assertThat(t.getConsumedAt()).isEqualTo(NOW));
        assertError(() -> auth.refresh(new RefreshTokenRequest(refresh)), INVALID_REFRESH_TOKEN);
        assertError(() -> passwords.resetPassword(new ResetPasswordRequest(firstReset, PASSWORD)), INVALID_PASSWORD_RESET_TOKEN);
        assertError(() -> passwords.resetPassword(new ResetPasswordRequest(secondReset, PASSWORD)), INVALID_PASSWORD_RESET_TOKEN);
        assertError(this::login, INVALID_CREDENTIALS);
        assertThat(auth.login(new LoginRequest(EMAIL, NEW_PASSWORD)).accessToken()).isNotBlank();
    }

    @Test
    void resetRejectsInvalidAndExpiredTokensWithoutChangingPassword() {
        UUID id = activate();
        String raw = requestReset();
        assertError(() -> passwords.resetPassword(new ResetPasswordRequest("missing", NEW_PASSWORD)), INVALID_PASSWORD_RESET_TOKEN);
        clock.now = NOW.plus(Duration.ofMinutes(30));
        assertError(() -> passwords.resetPassword(new ResetPasswordRequest(raw, NEW_PASSWORD)), EXPIRED_PASSWORD_RESET_TOKEN);
        assertThat(encoder.matches(PASSWORD, credentials.findByUserId(id).orElseThrow().getPasswordHash())).isTrue();
        assertThat(resets.findByTokenHash(tokens.hash(raw)).orElseThrow().getConsumedAt()).isNull();
    }

    @Test
    void passwordChangeVerifiesCurrentPasswordAndRevokesSessionsAndResetLinks() {
        UUID id = activate();
        String refresh = login().refreshToken();
        String reset = requestReset();
        passwords.changePassword(id, new ChangePasswordRequest(PASSWORD, NEW_PASSWORD));
        assertThat(encoder.matches(NEW_PASSWORD, credentials.findByUserId(id).orElseThrow().getPasswordHash())).isTrue();
        assertError(() -> auth.refresh(new RefreshTokenRequest(refresh)), INVALID_REFRESH_TOKEN);
        assertError(() -> passwords.resetPassword(new ResetPasswordRequest(reset, PASSWORD)), INVALID_PASSWORD_RESET_TOKEN);
    }

    @Test
    void passwordChangeRejectsWrongCurrentAndIdenticalNewPassword() {
        UUID id = activate();
        assertError(() -> passwords.changePassword(id, new ChangePasswordRequest("incorrect-password", NEW_PASSWORD)),
                CURRENT_PASSWORD_INVALID);
        assertError(() -> passwords.changePassword(id, new ChangePasswordRequest(PASSWORD, PASSWORD)),
                NEW_PASSWORD_SAME_AS_CURRENT);
        assertThat(encoder.matches(PASSWORD, credentials.findByUserId(id).orElseThrow().getPasswordHash())).isTrue();
    }

    @Test
    void fullLongUnicodePasswordIsHashedWithoutTruncation() {
        String longPassword = "é".repeat(128);
        RegisterResponse result = auth.register(new RegisterRequest(EMAIL, longPassword));
        verification.verify(new VerifyEmailRequest(delivery.last().token()));
        assertThat(auth.login(new LoginRequest(EMAIL, longPassword)).accessToken()).isNotBlank();
        assertError(() -> auth.login(new LoginRequest(EMAIL, "é".repeat(127) + "x")), INVALID_CREDENTIALS);
        assertThat(credentials.findByUserId(result.userId())).isPresent();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"email\":\"bad\",\"password\":\"valid-password\"}",
            "{\"email\":\"person@example.test\",\"password\":\"short\"}",
            "{\"email\":\"person@example.test\"}",
            "{\"password\":\"valid-password\"}",
            "{}"
    })
    void registrationValidationDoesNotLeakInput(String request) throws Exception {
        mvc.perform(post("/v1/public/auth/register").contentType("application/json").content(request))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("password"))));
        assertThat(users.count()).isZero();
    }

    @Test
    void oversizedPasswordIsRejectedAndServiceValidationAlsoApplies() throws Exception {
        mvc.perform(post("/v1/public/auth/register").contentType("application/json")
                        .content("{\"email\":\"person@example.test\",\"password\":\"" + "x".repeat(129) + "\"}"))
                .andExpect(status().isBadRequest());
        assertThatThrownBy(() -> auth.register(new RegisterRequest("invalid", PASSWORD)))
                .isInstanceOf(jakarta.validation.ConstraintViolationException.class);
    }

    @Test
    void publicRoutesReachValidationAndPrivateRoutesRequireBearerToken() throws Exception {
        for (String endpoint : List.of("register", "verify-email", "login", "refresh", "forgot-password", "reset-password")) {
            mvc.perform(post("/v1/public/auth/" + endpoint).contentType("application/json").content("{}"))
                    .andExpect(status().isBadRequest());
        }
        for (String endpoint : List.of("change-password", "logout")) {
            mvc.perform(post("/v1/auth/" + endpoint).contentType("application/json").content("{}"))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        }
    }

    @Test
    void protectedPasswordChangeUsesJwtSubject() throws Exception {
        UUID id = activate();
        AuthTokenResponse session = login();
        mvc.perform(post("/v1/auth/change-password")
                        .header("Authorization", "Bearer " + session.accessToken())
                        .contentType("application/json")
                        .content("{\"currentPassword\":\"initial-password\",\"newPassword\":\"updated-password\",\"userId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isOk());
        assertThat(encoder.matches(NEW_PASSWORD, credentials.findByUserId(id).orElseThrow().getPasswordHash())).isTrue();
    }

    @Test
    void jwtSignatureExpiryAndSubjectAreCheckedAndUnlistedRoutesAreDenied() throws Exception {
        activate();
        AuthTokenResponse session = login();
        mvc.perform(post("/v1/auth/logout").header("Authorization", "Bearer invalid")
                        .contentType("application/json").content("{\"refreshToken\":\"missing\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/unlisted").header("Authorization", "Bearer " + session.accessToken()))
                .andExpect(status().isForbidden());
        String malformedIdentity = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder().subject("not-a-uuid").claim("email", EMAIL)
                        .issuedAt(NOW).expiresAt(NOW.plusSeconds(900)).build())).getTokenValue();
        mvc.perform(post("/v1/auth/logout").header("Authorization", "Bearer " + malformedIdentity)
                        .contentType("application/json").content("{\"refreshToken\":\"missing\"}"))
                .andExpect(status().isUnauthorized());
        clock.now = NOW.plusSeconds(900);
        mvc.perform(post("/v1/auth/logout").header("Authorization", "Bearer " + session.accessToken())
                        .contentType("application/json").content("{\"refreshToken\":\"missing\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void httpFlowCoversRegistrationActivationRotationResetChangeAndLogout() throws Exception {
        mvc.perform(post("/v1/public/auth/register").contentType("application/json")
                        .content("{\"email\":\" PERSON@EXAMPLE.TEST \",\"password\":\"initial-password\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"))
                .andExpect(jsonPath("$.token").doesNotExist());
        mvc.perform(post("/v1/public/auth/verify-email").contentType("application/json")
                        .content("{\"token\":\"" + delivery.last().token() + "\"}"))
                .andExpect(status().isOk());
        String loginBody = mvc.perform(post("/v1/public/auth/login").contentType("application/json")
                        .content("{\"email\":\"person@example.test\",\"password\":\"initial-password\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String refresh = com.jayway.jsonpath.JsonPath.read(loginBody, "$.refreshToken");
        mvc.perform(post("/v1/public/auth/refresh").contentType("application/json")
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tokenType").value("Bearer"));
        String reset = requestReset();
        mvc.perform(post("/v1/public/auth/reset-password").contentType("application/json")
                        .content("{\"token\":\"" + reset + "\",\"newPassword\":\"updated-password\"}"))
                .andExpect(status().isOk());
        AuthTokenResponse updated = auth.login(new LoginRequest(EMAIL, NEW_PASSWORD));
        mvc.perform(post("/v1/auth/change-password")
                        .header("Authorization", "Bearer " + updated.accessToken())
                        .contentType("application/json")
                        .content("{\"currentPassword\":\"updated-password\",\"newPassword\":\"another-password\"}"))
                .andExpect(status().isOk());
        AuthTokenResponse latest = auth.login(new LoginRequest(EMAIL, "another-password"));
        mvc.perform(post("/v1/auth/logout").header("Authorization", "Bearer " + latest.accessToken())
                        .contentType("application/json").content("{\"refreshToken\":\"" + latest.refreshToken() + "\"}"))
                .andExpect(status().isOk());
        assertError(() -> auth.refresh(new RefreshTokenRequest(latest.refreshToken())), INVALID_REFRESH_TOKEN);
    }

    @Test
    void migrationsHaveRunAndForeignKeysAndUniqueCredentialsAreEnforced() {
        UUID id = activate();
        assertThat(jdbc.queryForObject("select count(*) from databasechangelog", Integer.class)).isEqualTo(5);
        assertThatThrownBy(() -> credentials.saveAndFlush(new UserCredential(id, "test-hash", NOW)))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(() -> credentials.saveAndFlush(new UserCredential(UUID.randomUUID(), "test-hash", NOW)))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    private RegisterResponse register() { return auth.register(new RegisterRequest(EMAIL, PASSWORD)); }

    private UUID activate() {
        RegisterResponse result = register();
        verification.verify(new VerifyEmailRequest(delivery.last().token()));
        return result.userId();
    }

    private AuthTokenResponse login() { return auth.login(new LoginRequest(EMAIL, PASSWORD)); }

    private String requestReset() {
        passwords.forgotPassword(new ForgotPasswordRequest(EMAIL));
        return delivery.last().token();
    }

    private static void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, AuthError error) {
        assertThatThrownBy(action).isInstanceOf(AuthException.class)
                .satisfies(ex -> assertThat(((AuthException) ex).getError()).isEqualTo(error));
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfiguration {
        @Bean @Primary MutableClock testClock() { return new MutableClock(); }
        @Bean DeliveryCollector deliveryCollector() { return new DeliveryCollector(); }
    }

    static class MutableClock extends Clock {
        volatile Instant now = NOW;
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    static class DeliveryCollector {
        final List<TokenDeliveryRequested> events = new CopyOnWriteArrayList<>();
        @TransactionalEventListener
        public void receive(TokenDeliveryRequested event) { events.add(event); }
        TokenDeliveryRequested last() { return events.getLast(); }
    }
}
