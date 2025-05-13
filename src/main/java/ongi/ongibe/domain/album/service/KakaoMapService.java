package ongi.ongibe.domain.album.service;

import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.dto.KakaoAddressDTO;
import ongi.ongibe.domain.album.dto.KakaoReverseGeocodeDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class KakaoMapService {

    @Value("${spring.kakao.auth.client}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public KakaoAddressDTO reverseGeocode(double lat, double lon) {
        String url = String.format("https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?x=%f&y=%f", lon, lat);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);

        var response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
                KakaoReverseGeocodeDTO.class);

        log.info("response body : {}", response.getBody().toString());

        var address = response.getBody().documents().stream()
                .filter(doc -> doc.region_type().equals("B"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("주소 없음"));

        return new KakaoAddressDTO(
                address.region_1depth_name(),
                address.region_2depth_name(),
                address.region_3depth_name());
    }
}
