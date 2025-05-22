package ongi.ongibe.domain.ai.service;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreRequestDTO;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreResponseDTO;
import ongi.ongibe.domain.ai.dto.AiImageRequestDTO;
import ongi.ongibe.domain.ai.dto.CategoryResponseDTO;
import ongi.ongibe.domain.ai.dto.DuplicateResponseDTO;
import ongi.ongibe.domain.ai.dto.ShakyResponseDTO;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiClient {

    private final WebClient webClient;
    private final PictureRepository pictureRepository;
    private final EntityManager entityManager;

    @Value("${ai.server.base-url}")
    private String baseUrl;

    private static final String EMBEDDING_PATH = "/api/albums/embedding";
    private static final String QUALITY_PATH = "/api/albums/quality";
    private static final String DUPLICATE_PATH = "/api/albums/duplicates";
    private static final String CATEGORY_PATH = "/api/albums/categories";
    private static final String SCORE_PATH = "/api/albums/score";
    private static final int MAX_ATTEMPTS = 3;

    public void requestEmbeddings(List<String> urls) {
        try {
            postJsonWithRetry(EMBEDDING_PATH, new AiImageRequestDTO(urls), Void.class);
            log.info("[AI] 임베딩 성공: {}", urls);
        } catch (Exception e) {
            log.error("[AI] 임베딩 실패: {}", urls, e);
            throw new RuntimeException("임베딩 실패로 재시도 중단", e);
        }
    }

    @Transactional
    public void requestQuality(Long albumId, List<String> urls) {
        log.info("[AI] requestQuality 호출됨, urls 개수: {}, url: {}", urls.size(), urls);
        var response = postJson(QUALITY_PATH, new AiImageRequestDTO(urls), ShakyResponseDTO.class);
        log.info("[AI] 품질 분석 응답: {}", response);
        if (response == null || response.data() == null) return;
        List<String> shakyUrls = response.data().stream()
                .filter(urls::contains)
                .toList();

        int shakyCount = pictureRepository.markPicturesAsShaky(albumId, shakyUrls);
        log.info("[AI] 흔들린 사진 : {}", shakyCount);
        entityManager.clear();
    }

    public void requestDuplicates(Long albumId, List<String> urls){
        log.info("[AI] requestDuplicates API 호출됨, urls 개수: {}, url: {}", urls.size(), urls);
        var response = postJsonWithRetry(DUPLICATE_PATH, new AiImageRequestDTO(urls), DuplicateResponseDTO.class);
        log.info("[AI] 중복 분석 응답: {}", response);
        if (response == null || response.data() == null) return;
        updateDuplicates(albumId, response.data());
    }

    @Transactional
    public void updateDuplicates(Long albumId, List<List<String>> data) {
        List<String> duplicatedUrls = data.stream()
                .flatMap(List::stream)
                .toList();

        int duplicatedCount = pictureRepository.markPicturesAsDuplicated(albumId, duplicatedUrls);
        log.info("[AI] 중복 사진 : {}", duplicatedCount);
        entityManager.clear();
    }

    @Transactional
    public void requestCategories(Long albumId, List<String> urls) {
        log.info("[AI] requestCategories API 호출됨, urls 개수: {}, url: {}", urls.size(), urls);
        List<Picture> pictures = pictureRepository.findAllByS3KeyIn(urls);
        log.info("[AI] findAllByS3KeyIn -> {}개 결과 반환", pictures.size());
        var response = postJsonWithRetry(CATEGORY_PATH, new AiImageRequestDTO(urls), CategoryResponseDTO.class);
        log.info("[AI] 카테고리 분석 응답: {}", response);
        if (response == null || response.data() == null) return;

        int totalTagUpdated = 0;
        for (var categoryResult : response.data()) {
            int count = pictureRepository.updateTag(albumId, categoryResult.images(), categoryResult.category());
            totalTagUpdated += count;
        }
        log.info("[AI] tag 반영 : {}", totalTagUpdated);
        entityManager.clear();
    }

    @Transactional
    public void requestAestheticScore(Long albumId, List<String> keys) {
        log.info("[AI] requestAestheticScore API 호출됨, keys 개수: {}, url: {}", keys.size(), keys);
        List<Picture> pictures = pictureRepository.findAllByS3KeyIn(keys);
        log.info("첫번째 사진 tag : {}", pictures.getFirst().getTag());
        log.info("[AI] findAllByS3KeyIn -> {}개 결과 반환", pictures.size());

        var response = postJsonWithRetry(SCORE_PATH, AiAestheticScoreRequestDTO.from(pictures), AiAestheticScoreResponseDTO.class);
        log.info("[AI] 품질점수 분석 응답: {}", response);
        if (response == null || response.data() == null) return;

        int totalScoreUpdated = 0;
        for (var category : response.data()) {
            for (var entry : category.images()){
                int count = pictureRepository.updateScore(albumId, entry.image(), entry.score());
                totalScoreUpdated += count;
            }
        }
        log.info("[AI] score 반영 : {}", totalScoreUpdated);
        entityManager.clear();
    }

    private <T, R> R postJson(String path, T body, Class<R> responseType) {
        String url = baseUrl + path;
        try {
            log.info("[AI] 요청 보내는 중: {} with body = {}", url, body);
            R response = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();

            log.info("[AI] 응답 수신: {} => {}", url, response);
            return response;
        } catch (Exception e) {
            log.error("[AI] 요청 실패: {} with body = {}", url, body, e);
            throw e;
        }
    }

    private <T, R> R postJsonWithRetry(String path, T body, Class<R> responseType) {
        String url = baseUrl + path;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                log.info("[AI][시도 {}] 요청: {} with body = {}", attempt, url, body);
                return webClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(body)
                        .retrieve()
                        .onStatus(status -> status.value() == 422, response -> {
                            log.warn("[AI] 422 응답 발생: {}, 임베딩 재요청 후 재시도", url);
                            if (body instanceof AiImageRequestDTO(List<String> images)) {
                                requestEmbeddings(images); // 다시 임베딩
                            }
                            return response.createException(); // 계속 예외 흐름 유지
                        })
                        .bodyToMono(responseType)
                        .block();
            } catch (Exception e) {
                log.error("[AI] 요청 실패 (시도 {}): {} with body = {}", attempt, url, body, e);
                if (attempt == MAX_ATTEMPTS) throw e;
                log.warn("[AI] 재시도 준비 중 (남은 시도: {})", MAX_ATTEMPTS - attempt);
            }
        }
        throw new IllegalStateException("예외가 발생하지 않고 종료될 수 없습니다.");
    }

}
