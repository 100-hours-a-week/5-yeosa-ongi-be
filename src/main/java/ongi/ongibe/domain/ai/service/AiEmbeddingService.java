package ongi.ongibe.domain.ai.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiEmbeddingService {

    private final AiClient aiClient;

    public void requestEmbeddings(List<String> s3keys) {
        aiClient.requestEmbeddings(s3keys);
    }
}
