package ongi.ongibe.domain.album.schedule;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.event.AlbumEvent;
import ongi.ongibe.domain.album.event.AlbumRetryEvent;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.PictureRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlbumAiRetryScheduler {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedRate = 60 * 1000) // 1분에 한번
    public void retryAlbumProcess(){
        List<Album> retryTargets = albumRepository
                .findByProcessStateIn(List.of(AlbumProcessState.FAILED, AlbumProcessState.NOT_STARTED));

        for (Album album : retryTargets) {
            try {
                List<Picture> pictures = pictureRepository.findAllByAlbum(album);
                List<String> pictureKeys = pictures.stream()
                        .map(Picture::getS3Key)
                        .toList();

                album.setProcessState(AlbumProcessState.IN_PROGRESS);
                log.info("[AI 재시도] 앨범 ID: {}, 사진 수: {}", album.getId(), pictureKeys.size());
                eventPublisher.publishEvent(new AlbumRetryEvent(album.getId(), pictureKeys));
            } catch (Exception e) {
                log.error("[AI 재시도 실패] 앨범 ID: {}, message: {}", album.getId(), e.getMessage(), e);
                album.setProcessState(AlbumProcessState.FAILED);
            }
        }
    }
}
