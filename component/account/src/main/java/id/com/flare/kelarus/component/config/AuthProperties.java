package id.com.flare.kelarus.component.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("kelarus.auth")
public record AuthProperties(
        String jwtSecret,
        @DefaultValue("15m") Duration accessTokenTtl,
        @DefaultValue("30d") Duration refreshTokenTtl,
        @DefaultValue("24h") Duration emailVerificationTtl,
        @DefaultValue("30m") Duration passwordResetTtl) {

    public AuthProperties {
        for (Duration ttl : new Duration[]{accessTokenTtl, refreshTokenTtl, emailVerificationTtl, passwordResetTtl}) {
            if (ttl == null || ttl.compareTo(Duration.ofSeconds(1)) < 0) {
                throw new IllegalArgumentException("Authentication token TTLs must be at least one second.");
            }
        }
    }

    @Override
    public String toString() { return "AuthProperties[REDACTED]"; }
}
