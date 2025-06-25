package ongi.ongibe.domain.album.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.aiInterface.AiAlbumServiceInterface;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.repository.AlbumRepository;
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
    private final AiAlbumServiceInterface aiAlbumService;
    private final AlbumRepository albumRepository;

    @Async("asyncExecutor")
    public void processAlbumAsync(Long albumId, Long userId, List<String> pictureS3Key) {
        processAlbumTransaction(albumId, userId, pictureS3Key);
    }

    @Transactional
    public void processAlbumTransaction(Long albumId, Long userId, List<String> pictureS3Keys) {
        geoService.geoAndKakaoAndSave(albumId, pictureS3Keys);
        Album album = albumRepository.findById(albumId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "앨범을 찾을 수 없습니다.")
        );
        try{
            album.setProcessState(AlbumProcessState.IN_PROGRESS);
            albumRepository.save(album);
            log.info("AiAlbumServiceInterface = {}", aiAlbumService.getClass());
            aiAlbumService.process(album, userId, pictureS3Keys);
        } catch (Exception e) {
            album.setProcessState(AlbumProcessState.FAILED);
            albumRepository.save(album);
        }
    }
}