package id.com.flare.kelarus.component.repository;

import id.com.flare.kelarus.component.domain.EmailVerification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {

	@Query("select t.userId from EmailVerification t where t.tokenHash = :hash")
	Optional<UUID> findUserIdByTokenHash(@Param("hash") String hash);

	// Read only after acquiring the owning user's lock.
	Optional<EmailVerification> findByTokenHash(String tokenHash);

}
