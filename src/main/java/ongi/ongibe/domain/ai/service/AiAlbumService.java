package ongi.ongibe.domain.ai.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiAlbumService {

    private final AiClient aiClient;

    public void process(Album album) {
        List<Picture> pictures = album.getPictures();
        List<String> urls = pictures.stream()
                .map(Picture::getPictureURL)
                .toList();

        // 1. 임베딩 요청 (선행 조건)
        aiClient.requestEmbeddings(urls);

        // 2. 병렬 요청 실행 (quality, duplicates, categories)
        CompletableFuture<Void> quality = CompletableFuture.runAsync(() ->
                aiClient.requestQuality(urls));

        CompletableFuture<Void> duplicates = CompletableFuture.runAsync(() ->
                aiClient.requestDuplicates(urls));

        CompletableFuture<Void> categories = CompletableFuture.runAsync(() ->
                aiClient.requestCategories(urls));

        // 3. 병렬 작업 완료 후 aestheticScore 후행 처리
        CompletableFuture.allOf(quality, duplicates, categories).thenRun(() ->
                aiClient.requestAestheticScore(urls)
        ).join();
    }
}
