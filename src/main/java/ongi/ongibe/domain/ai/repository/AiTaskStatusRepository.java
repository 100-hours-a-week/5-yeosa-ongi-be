package ongi.ongibe.domain.ai.repository;

import ongi.ongibe.domain.ai.entity.AiTaskStatus;
import ongi.ongibe.domain.ai.AiStatus;
import ongi.ongibe.domain.ai.repository.queryDSL.AiTaskStatusRepositoryQueryDSL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AiTaskStatusRepository extends JpaRepository<AiTaskStatus, String>,
        AiTaskStatusRepositoryQueryDSL {
}
