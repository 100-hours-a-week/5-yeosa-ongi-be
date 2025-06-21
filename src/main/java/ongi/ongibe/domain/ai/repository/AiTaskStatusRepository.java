package ongi.ongibe.domain.ai.repository;

import ongi.ongibe.domain.ai.entity.AiTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiTaskStatusRepository extends JpaRepository<AiTaskStatus, String> {

}
