package ongi.ongibe.domain.album.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.AlbumProcessState;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.exception.AlbumException;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumMarkService {

    private final AlbumRepository albumRepository;

    @Transactional
    public void markProcess(Long albumId, AlbumProcessState processState) {
        System.out.println(">> 진입 시도 - albumId = " + albumId + ", state = " + processState);
        log.info("[DEBUG] markProcess 진입 - albumId={}, state={}", albumId, processState);
        try {
            Album album = albumRepository.findById(albumId)
                    .orElseThrow(() -> new AlbumException(HttpStatus.NOT_FOUND, "album not found..........."));
            log.info("[DEBUG] albumId={} 상태 = {}", album.getId(), album.getProcessState());
            album.setProcessState(processState);
        } catch (AlbumException e) {
            System.out.println(">>>> AlbumException 발생 - " + e.getReason());
            log.error("❌ markProcess 실패 - albumId={}, reason={}", albumId, e.getReason());
            throw e;
        }
    }

}
