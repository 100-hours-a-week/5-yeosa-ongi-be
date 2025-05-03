package ongi.ongibe.domain.ai.service;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AiClient {

    private final RestTemplate restTemplate;
    private final PictureRepository pictureRepository;
    private final EntityManager entityManager;

    @Value("${ai.server.base-url}")
    private String baseUrl;

    private static final String EMBEDDING_PATH = "/api/albums/embedding";
    private static final String QUALITY_PATH = "/api/albums/quality";
    private static final String DUPLICATE_PATH = "/api/albums/duplicates";
    private static final String CATEGORY_PATH = "/api/albums/categories";
    private static final String SCORE_PATH = "/api/albums/score";

    public void requestEmbeddings(List<String> urls) {
        postJson(EMBEDDING_PATH, new AiImageRequestDTO(urls), Void.class);
    }

    public void requestQuality(List<String> urls) {
        log.info("[AI] requestQuality 호출됨, urls 개수: {}, url: {}", urls.size(), urls);
        var response = postJson(QUALITY_PATH, new AiImageRequestDTO(urls), ShakyResponseDTO.class);
        log.info("[AI] 품질 분석 응답: {}", response);
        if (response == null || response.data() == null) return;
        List<String> shakyUrls = response.data().stream()
                .filter(urls::contains)
                .toList();

        int shakyCount = pictureRepository.markPicturesAsShaky(shakyUrls);
        log.info("[AI] 흔들린 사진 : {}", shakyCount);
        entityManager.clear();
    }

    public void requestDuplicates(List<String> urls) {
        log.info("[AI] requestDuplicates API 호출됨, urls 개수: {}, url: {}", urls.size(), urls);
        var response = postJson(DUPLICATE_PATH, new AiImageRequestDTO(urls), DuplicateResponseDTO.class);
        if (response == null || response.data() == null) return;
        List<String> duplicatedUrls = response.data().stream()
                .flatMap(List::stream)
                .toList();

        int duplicatedCount = pictureRepository.markPicturesAsDuplicated(duplicatedUrls);
        log.info("[AI] 중복 사진 : {}", duplicatedCount);
        entityManager.clear();
    }

    public void requestCategories(List<String> urls) {
        log.info("[AI] requestCategories API 호출됨, urls 개수: {}, url: {}", urls.size(), urls);
        List<Picture> pictures = pictureRepository.findAllByPictureURLIn(urls);
        log.info("[AI] findAllByPictureURLIn -> {}개 결과 반환", pictures.size());
        var response = postJson(CATEGORY_PATH, new AiImageRequestDTO(urls), CategoryResponseDTO.class);
        if (response == null || response.data() == null) return;

        int totalTagUpdated = 0;
        for (var categoryResult : response.data()) {
            int count = pictureRepository.updateTagIfAbsent(categoryResult.images(), categoryResult.category());
            totalTagUpdated += count;
        }
        log.info("[AI] tag 반영 : {}", totalTagUpdated);
        entityManager.clear();
    }

    public void requestAestheticScore(List<String> urls) {
        log.info("[AI] requestAestheticScore API 호출됨, urls 개수: {}, url: {}", urls.size(), urls);
        List<Picture> pictures = pictureRepository.findAllByPictureURLIn(urls);
        log.info("[AI] findAllByPictureURLIn -> {}개 결과 반환", pictures.size());

        var response = postJson(SCORE_PATH, AiAestheticScoreRequestDTO.from(pictures), AiAestheticScoreResponseDTO.class);
        if (response == null || response.data() == null) return;

        int totalScoreUpdated = 0;
        for (var category : response.data()) {
            for (var entry : category.images()){
                int count = pictureRepository.updateScore(entry.image(), entry.score());
                totalScoreUpdated += count;
            }
        }
        log.info("[AI] score 반영 : {}", totalScoreUpdated);
        entityManager.clear();
    }


    private Map<String, Picture> toMap(List<Picture> pictures) {
        return pictures.stream().collect(
                Collectors.toMap(Picture::getPictureURL, p -> p)
        );
    }

    private <T, R> R postJson(String path, T body, Class<R> responseType) {
        String url = baseUrl + path;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<T> request = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(url, request, responseType);
    }

}
