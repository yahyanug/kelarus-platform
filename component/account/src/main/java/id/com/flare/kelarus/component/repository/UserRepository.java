package id.com.flare.kelarus.component.repository;

import id.com.flare.kelarus.component.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

	boolean existsByEmail(String email);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<User> findByEmail(String email);

	// All credential/token mutations acquire this lock before loading related rows.
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from User u where u.id = :id")
	Optional<User> findLockedById(@Param("id") UUID id);

}
