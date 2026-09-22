package id.com.flare.kelarus.component.repository;

import id.com.flare.kelarus.component.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

	@Query("select t.userId from PasswordResetToken t where t.tokenHash = :hash")
	Optional<UUID> findUserIdByTokenHash(@Param("hash") String hash);

	// Read only after acquiring the owning user's lock.
	Optional<PasswordResetToken> findByTokenHash(String tokenHash);

	@Modifying
	@Query("update PasswordResetToken t set t.consumedAt = :now where t.userId = :userId and t.consumedAt is null")
	int consumeAllByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

}
