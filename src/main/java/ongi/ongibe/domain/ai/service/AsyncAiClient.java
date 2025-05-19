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
    public void requestQuality(Long albumId, List<String> urls) {
        aiClient.requestQuality(albumId, urls);
    }

    @Async("asyncExecutor")
    public void requestDuplicates(Long albumId, List<String> urls) {
        aiClient.requestDuplicates(albumId, urls);
    }

    @Async("asyncExecutor")
    public void requestCategories(Long albumId, List<String> urls) {
        aiClient.requestCategories(albumId, urls);
    }

    @Async("asyncExecutor")
    public void requestAestheticScore(Long albumId, List<String> urls) {
        aiClient.requestAestheticScore(albumId, urls);
    }
}

