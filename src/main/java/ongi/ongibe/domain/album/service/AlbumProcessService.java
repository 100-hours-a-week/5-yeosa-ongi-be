package ongi.ongibe.domain.album.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.ai.service.AiAlbumService;
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
    private final AiAlbumService aiAlbumService;
    private final AlbumRepository albumRepository;

    @Async("asyncExecutor")
    public void processAlbumAsync(Long albumId, List<String> pictureS3Key) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "앨범을 찾을 수 없습니다."));

        List<Long> memberIds = album.getUserAlbums().stream()
                .map(ua -> ua.getUser().getId())
                .toList();

        processAlbumTransaction(album, pictureS3Key, memberIds);
    }

    @Transactional
    public void processAlbumTransaction(Album album, List<String> pictureS3Keys, List<Long> memberIds) {
//        List<Picture> pictures = geoService.geoAndKakaoAndSave(albumId, pictureS3Keys);
        try{
            album.changeProcessState(AlbumProcessState.IN_PROGRESS, memberIds);
            albumRepository.save(album);
            aiAlbumService.process(album, pictureS3Keys, memberIds);
        } catch (Exception e) {
            album.changeProcessState(AlbumProcessState.FAILED, memberIds);
            albumRepository.save(album);
        }
    }
}