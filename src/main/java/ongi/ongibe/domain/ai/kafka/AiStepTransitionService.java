package ongi.ongibe.domain.ai.kafka;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.AiStep;
import ongi.ongibe.domain.ai.entity.AiTaskStatus;
import ongi.ongibe.domain.ai.producer.AiAestheticScoreProducer;
import ongi.ongibe.domain.ai.producer.AiCategoryProducer;
import ongi.ongibe.domain.ai.producer.AiClusterProducer;
import ongi.ongibe.domain.ai.producer.AiDuplicateProducer;
import ongi.ongibe.domain.ai.producer.AiEmbeddingProducer;
import ongi.ongibe.domain.ai.producer.AiShakeProducer;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiStepTransitionService {

    private final AiEmbeddingProducer aiEmbeddingProducer;
    private final AiShakeProducer aiShakeProducer;
    private final AiDuplicateProducer aiDuplicateProducer;
    private final AiCategoryProducer aiCategoryProducer;
    private final AiClusterProducer aiClusterProducer;
    private final AiAestheticScoreProducer aiAestheticScoreProducer;

    public void handleStepCondition(AiTaskStatus taskStatus, List<String> s3keys) {
        AiStep step = taskStatus.getStep();
        Long albumId = taskStatus.getAlbumId();
        Long userId = taskStatus.getUserId();

        switch (step) {
            case EMBEDDING -> {
                aiShakeProducer.requestShaky(albumId, userId, s3keys);
                aiCategoryProducer.requestCategory(albumId, userId, s3keys);
                aiDuplicateProducer.requestDuplicate(albumId, userId, s3keys);
                aiClusterProducer.requestCluster(albumId, userId, s3keys);
            }
            case CATEGORY -> aiAestheticScoreProducer.requestAestheticScores(albumId, userId, s3keys);

            default -> log.info("[AI] Step {}는 후속 단계 없음", step);
        }
    }

}
