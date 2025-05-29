package ongi.ongibe.domain.ai.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiEmbeddingService {

    private final AiClient aiClient;

    public void requestEmbeddings(List<String> s3keys) {
        log.info("[AI] 임베딩 요청 시작");
        aiClient.requestEmbeddings(s3keys);
        log.info("[AI] 임베딩 요청 완료");
    }
}
