package ongi.ongibe.domain.ai.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncAiClient {

    private final AiClient aiClient;

    @Async("asyncExecutor")
    public void requestQuality(Long albumId, List<String> keys) {
        aiClient.requestQuality(albumId, keys);
    }

    @Async("asyncExecutor")
    public void requestDuplicates(Long albumId, List<String> keys) {
        aiClient.requestDuplicates(albumId, keys);
    }

    @Async("asyncExecutor")
    public void requestCategories(Long albumId, List<String> keys) {
        aiClient.requestCategories(albumId, keys);
    }
}

