package ongi.ongibe.domain.ai.service;

import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreRequestDTO;
import ongi.ongibe.domain.ai.dto.AiAestheticScoreResponseDTO;
import ongi.ongibe.domain.ai.dto.AiCategoryRequestDTO;
import ongi.ongibe.domain.ai.dto.AiClusterResponseDTO;
import ongi.ongibe.domain.ai.dto.AiImageRequestDTO;
import ongi.ongibe.domain.ai.dto.CategoryResponseDTO;
import ongi.ongibe.domain.ai.dto.DuplicateResponseDTO;
import ongi.ongibe.domain.ai.dto.ShakyResponseDTO;
import ongi.ongibe.domain.album.entity.FaceCluster;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.entity.PictureFaceCluster;
import ongi.ongibe.domain.album.repository.FaceClusterRepository;
import ongi.ongibe.domain.album.repository.PictureFaceClusterRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiClient {

    @Qualifier("webClient")
    private final WebClient webClient;

    @Qualifier("healthCheckWebClient")
    private final WebClient healthCheckWebClient;

    @Value("${ai.server.base-url}")
    private String baseUrl;

    private static final String HEALTH_INFO_PATH = "/health/info";
    private static final String EMBEDDING_PATH = "/api/albums/embedding";
    private static final String QUALITY_PATH = "/api/albums/quality";
    private static final String DUPLICATE_PATH = "/api/albums/duplicates";
    private static final String CATEGORY_PATH = "/api/albums/categories";
    private static final String SCORE_PATH = "/api/albums/score";
    private static final String PEOPLE_PATH = "/api/albums/people";
    private static final int MAX_ATTEMPTS = 3;

    public boolean isAiServerAvailable() {
        try {
            String response = healthCheckWebClient.get()
                    .uri(baseUrl + HEALTH_INFO_PATH)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(1)); // 타임아웃 설정

            log.info("current AI server : {}", baseUrl + HEALTH_INFO_PATH);
            return response != null && response.contains("ok");
        } catch (Exception e) {
            log.warn("AI 서버 헬스체크 실패", e);
            return false;
        }
    }

    public void requestEmbeddings(List<String> keys) {
        try {
            postJsonWithRetry(EMBEDDING_PATH, new AiImageRequestDTO(keys), Void.class);
        } catch (Exception e) {
            throw new RuntimeException("임베딩 실패로 재시도 중단", e);
        }
    }

    public List<String> getShakyKeys(Long albumId, List<String> keys) {
        var response = postJson(QUALITY_PATH, new AiImageRequestDTO(keys), ShakyResponseDTO.class);
        return response != null && response.data() != null ? response.data() : List.of();
    }

    public List<List<String>> getDuplicateGroups(Long albumId, List<String> keys) {
        var response = postJsonWithRetry(DUPLICATE_PATH, new AiImageRequestDTO(keys), DuplicateResponseDTO.class);
        return response != null && response.data() != null ? response.data() : List.of();
    }

    public List<CategoryResponseDTO.CategoryResult> getCategories(Long albumId, List<String> keys, List<String> concepts) {
        var response = postJsonWithRetry(CATEGORY_PATH, new AiCategoryRequestDTO(keys, concepts), CategoryResponseDTO.class);
        return response != null && response.data() != null ? response.data() : List.of();
    }

    public List<AiAestheticScoreResponseDTO.ScoreCategory> getAestheticScore(List<AiAestheticScoreRequestDTO.Category> categories) {
        var response = postJsonWithRetry(SCORE_PATH, new AiAestheticScoreRequestDTO(categories), AiAestheticScoreResponseDTO.class);
        return response != null && response.data() != null ? response.data() : List.of();
    }

    public List<AiClusterResponseDTO.ClusterData> getClusters(Long albumId, List<String> keys) {
        var response = postJson(PEOPLE_PATH, new AiImageRequestDTO(keys), AiClusterResponseDTO.class);
        return response != null && response.data() != null ? response.data() : List.of();
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
                return webClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(body)
                        .retrieve()
                        .onStatus(status -> status.value() == 422, response -> {
                            if (body instanceof AiImageRequestDTO(List<String> images)) {
                                requestEmbeddings(images); // 다시 임베딩
                            }
                            return response.createException(); // 계속 예외 흐름 유지
                        })
                        .bodyToMono(responseType)
                        .block();
            } catch (Exception e) {
                if (attempt == MAX_ATTEMPTS) throw e;
            }
        }
        throw new IllegalStateException("예외가 발생하지 않고 종료될 수 없습니다.");
    }

}
