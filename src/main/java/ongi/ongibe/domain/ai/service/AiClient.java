package ongi.ongibe.domain.ai.service;

import java.util.List;
import java.util.Map;
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
        log.info("[AI] 품질 분석 응답: {}", response);  // <- 이거 추가
        if (response == null || response.data() == null) return;
        List<Picture> pictures = pictureRepository.findAllByPictureURLIn(urls);
        log.info("[AI] findAllByPictureURLIn -> {}개 결과 반환", pictures.size());
        Map<String, Picture> map = toMap(pictures);
        response.data().forEach(urlStr -> {
            Picture p = map.get(urlStr);
            log.info("before markAsShaky: {} -> isShaky={}", p.getPictureURL(), p.isShaky());
            p.markAsShaky();
        });
        log.info("[AI] {}개 picture 저장 시작: {}", pictures.size(), urls);
        pictureRepository.saveAll(pictures);
        log.info("[AI] picture 저장 완료");
    }

    public void requestDuplicates(List<String> urls) {
        log.info("[AI] requestDuplicates API 호출됨, urls 개수: {}, url: {}", urls.size(), urls);
        List<Picture> pictures = pictureRepository.findAllByPictureURLIn(urls);
        log.info("[AI] findAllByPictureURLIn -> {}개 결과 반환", pictures.size());
        var response = postJson(DUPLICATE_PATH, new AiImageRequestDTO(urls), DuplicateResponseDTO.class);
        if (response == null || response.data() == null) return;

        Map<String, Picture> map = toMap(pictures);
        response.data().stream()
                .flatMap(List::stream)
                .distinct()
                .forEach(urlStr -> {
                    Picture p = map.get(urlStr);
                    if (p != null) p.markAsDuplicate();
                });
        log.info("[AI] {}개 picture 저장 시작: {}", pictures.size(), urls);
        pictureRepository.saveAll(pictures);
        log.info("[AI] picture 저장 완료");
    }

    public void requestCategories(List<String> urls) {
        log.info("[AI] requestCategories API 호출됨, urls 개수: {}, url: {}", urls.size(), urls);
        List<Picture> pictures = pictureRepository.findAllByPictureURLIn(urls);
        log.info("[AI] findAllByPictureURLIn -> {}개 결과 반환", pictures.size());
        var response = postJson(CATEGORY_PATH, new AiImageRequestDTO(urls), CategoryResponseDTO.class);
        if (response == null || response.data() == null) return;

        Map<String, Picture> map = toMap(pictures);
        for (var category : response.data()) {
            for (String urlStr : category.images()) {
                Picture p = map.get(urlStr);
                if (p != null) p.setTagIfAbsent(category.category());
            }
        }
        log.info("[AI] {}개 picture 저장 시작: {}", pictures.size(), urls);
        pictureRepository.saveAll(pictures);
        log.info("[AI] picture 저장 완료");
    }

    public void requestAestheticScore(List<String> urls) {
        log.info("[AI] requestAestheticScore API 호출됨, urls 개수: {}, url: {}", urls.size(), urls);
        List<Picture> pictures = pictureRepository.findAllByPictureURLIn(urls);
        log.info("[AI] findAllByPictureURLIn -> {}개 결과 반환", pictures.size());

        AiAestheticScoreRequestDTO request = AiAestheticScoreRequestDTO.from(pictures);
        var response = postJson(SCORE_PATH, request, AiAestheticScoreResponseDTO.class);
        if (response == null || response.data() == null) return;

        Map<String, Picture> map = toMap(pictures);
        for (var category : response.data()) {
            for (var entry : category.images()) {
                Picture p = map.get(entry.image());
                if (p != null) {
                    p.applyAestheticScore(entry.score());
                    p.setTagIfAbsent(category.category());
                }
            }
        }
        log.info("[AI] {}개 picture 저장 시작: {}", pictures.size(), urls);
        pictureRepository.saveAll(pictures);
        log.info("[AI] picture 저장 완료");
    }


    private Map<String, Picture> toMap(List<Picture> pictures) {
        return pictures.stream().collect(Collectors.toMap(Picture::getPictureURL, p -> p));
    }

    private <T, R> R postJson(String path, T body, Class<R> responseType) {
        String url = baseUrl + path + "/";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<T> request = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(url, request, responseType);
    }

}
