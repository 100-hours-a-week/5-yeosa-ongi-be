package ongi.ongibe.domain.album.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.service.AiAlbumService;
import ongi.ongibe.domain.album.AlbumProcessState;
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
    private final AlbumRepository albumRepository;

    @Async("asyncExecutor")
    public void processAlbumAsync(Long albumId, List<String> pictureS3Keys) {
        List<Picture> pictures = geoService.geoAndKakaoAndSave(albumId, pictureS3Keys);
        Album album = albumRepository.findById(albumId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "앨범을 찾을 수 없습니다.")
        );
        try{
            album.setProcessState(AlbumProcessState.IN_PROGRESS);
            albumRepository.save(album);
            aiAlbumService.process(album, pictures);
        } catch (Exception e) {
            album.setProcessState(AlbumProcessState.FAILED);
            albumRepository.save(album);
        }
    }
}