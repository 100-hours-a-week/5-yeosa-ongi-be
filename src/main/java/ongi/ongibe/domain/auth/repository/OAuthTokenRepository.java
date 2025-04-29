package ongi.ongibe.domain.auth.repository;

import ongi.ongibe.domain.auth.entity.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {

}
