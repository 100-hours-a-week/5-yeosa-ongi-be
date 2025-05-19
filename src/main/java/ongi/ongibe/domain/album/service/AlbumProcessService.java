package ongi.ongibe.domain.album.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.service.AiAlbumService;
import ongi.ongibe.domain.album.dto.KakaoAddressDTO;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.place.entity.Place;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import ongi.ongibe.domain.album.repository.PlaceRepository;
import ongi.ongibe.domain.place.service.PlaceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumProcessService {

    private final GeoService geoService;
    private final AiAlbumService aiAlbumService;


    @Async("asyncExecutor")
    public void processAlbumAsync(Long albumId, List<String> pictureUrls) {
        List<Picture> pictures = geoService.geoAndKakaoAndSave(albumId, pictureUrls);
        try{
            aiAlbumService.process(albumId, pictures);
        } catch (Exception e) {
            log.error("[AI 처리 실패] pictureUrls: {}, message: {}", pictureUrls, e.getMessage(), e);
        }
    }
}