package ongi.ongibe.domain.album.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.place.entity.Place;
import ongi.ongibe.domain.place.service.PlaceService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final S3MetadataService s3MetadataService;
    private final KakaoMapService kakaoMapService;
    private final PlaceService placeService;

    @Transactional
    public List<Picture> geoAndKakaoAndSave(Long albumId, List<String> s3keys) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "앨범 없음"));

        List<Picture> pictures = pictureRepository.findAllByS3KeyIn(s3keys).stream()
                .filter(p -> p.getAlbum().getId().equals(albumId))
                .toList();

        for (Picture p : pictures) {
//            var gps = s3MetadataService.extractGPS(p.getPictureURL());
//            p.setLatitude(gps.lat());
//            p.setLongitude(gps.lon());
//            log.info("lat: {}, lon: {}", gps.lat(), gps.lon());
            Double longitude = p.getLongitude();
            Double latitude = p.getLatitude();
            if (isInKorea(latitude, longitude)) {
                try{
                    var address = kakaoMapService.reverseGeocode(latitude, longitude);
                    Place place = placeService.findOrCreate(address);
                    p.setPlace(place);
                } catch (Exception e){
                    log.warn("Kakao API 실패 - pictureId: {}, lat: {}, lon: {}, message: {}",
                            p.getId(), latitude, longitude, e.getMessage(), e);
                }
            } else {
                log.info("대한민국 범위 밖 또는 좌표 없음 - pictureId: {}, lat: {}, lon: {}",
                        p.getId(), latitude, longitude);
            }
        }
        return pictureRepository.saveAll(pictures);
    }

    private boolean isInKorea(Double lat, Double lon) {
        return lat != null && lon != null &&
                lat >= 33.0 && lat <= 39.0 &&
                lon >= 124.0 && lon <= 132.0;
    }
}
