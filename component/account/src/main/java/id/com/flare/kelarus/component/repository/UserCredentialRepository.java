package id.com.flare.kelarus.component.repository;

import id.com.flare.kelarus.component.domain.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserCredentialRepository extends JpaRepository<UserCredential, UUID> {

	Optional<UserCredential> findByUserId(UUID userId);

}
