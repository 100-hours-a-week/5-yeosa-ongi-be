package ongi.ongibe.domain.user.repository;

import java.util.Optional;
import ongi.ongibe.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderId(String providerId);
}
