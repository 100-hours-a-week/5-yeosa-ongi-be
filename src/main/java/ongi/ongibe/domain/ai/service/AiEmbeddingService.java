package ongi.ongibe.domain.ai.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.aiInterface.AiEmbeddingServiceInterface;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiEmbeddingService implements AiEmbeddingServiceInterface {

    private final AiClient aiClient;

    @Override
    public void requestEmbeddings(Long albumId, Long userId, List<String> s3keys) {
        log.info("[AI] 임베딩 요청 시작");
        aiClient.requestEmbeddings(s3keys);
        log.info("[AI] 임베딩 요청 완료");
    }
}
