package ongi.ongibe.domain.album.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.dto.KakaoAddressDTO;
import ongi.ongibe.domain.album.dto.KakaoReverseGeocodeDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoMapService {

    @Value("${spring.kakao.auth.client}")
    private String kakaoApiKey;

    private final WebClient webClient;

    public KakaoAddressDTO reverseGeocode(double lat, double lon) {
        String url = String.format(
                "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?x=%f&y=%f", lon, lat);

        var response = webClient.get()
                .uri(url)
                .header("Authorization", "KakaoAK " + kakaoApiKey)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(KakaoReverseGeocodeDTO.class)
                .block();

        if (response == null) {
            throw new RuntimeException("카카오 응답이 비정상적입니다");
        }

            log.info("response body : {}", response.documents());

            var address = response.documents().stream()
                    .filter(doc -> doc.region_type().equals("B"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("주소 없음"));

            return new KakaoAddressDTO(
                    address.region_1depth_name(),
                    address.region_2depth_name(),
                    address.region_3depth_name());
        }

}
