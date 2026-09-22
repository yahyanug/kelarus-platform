package id.com.flare.kelarus.component.security;

import id.com.flare.kelarus.component.config.AuthProperties;
import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SecurityConfigurationTest {
    private final SecurityConfiguration configuration = new SecurityConfiguration();

    @Test
    void signingKeyRejectsMissingMalformedAndShortSecretsWithoutEchoingThem() {
        for (String value : new String[]{"", "not-base64!", Base64.getEncoder().encodeToString(new byte[16])}) {
            assertThatThrownBy(() -> configuration.jwtSigningKey(properties(value)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("KELARUS_AUTH_JWT_SECRET");
        }
        assertThatThrownBy(() -> configuration.jwtSigningKey(properties(null)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void signingKeyAcceptsAtLeast256BitsAndConfigurationToStringRedactsSecret() {
        byte[] random = new byte[32];
        new java.security.SecureRandom().nextBytes(random);
        String encoded = Base64.getEncoder().encodeToString(random);
        assertThat(configuration.jwtSigningKey(properties(encoded)).getEncoded()).containsExactly(random);
        assertThat(properties(encoded).toString()).doesNotContain(encoded);
    }

    @Test
    void tokenTtlsMustBePositiveAndAtLeastOneSecond() {
        assertThatThrownBy(() -> new AuthProperties(null, Duration.ZERO, Duration.ofDays(30),
                Duration.ofHours(24), Duration.ofMinutes(30))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AuthProperties(null, Duration.ofMillis(999), Duration.ofDays(30),
                Duration.ofHours(24), Duration.ofMinutes(30))).isInstanceOf(IllegalArgumentException.class);
    }

    private AuthProperties properties(String secret) {
        return new AuthProperties(secret, Duration.ofMinutes(15), Duration.ofDays(30),
                Duration.ofHours(24), Duration.ofMinutes(30));
    }
}
