package ongi.ongibe.domain.album.exception;

import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.common.BaseApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class AlbumExceptionHandler {

    @ExceptionHandler(AlbumException.class)
    public ResponseEntity<BaseApiResponse<Void>> handleAlbumException(AlbumException e) {
        log.warn("앨범 오류: {}", e.getReason());
        String code = switch (e.getStatusCode()) {
            case HttpStatus.NOT_FOUND -> "ALBUM_NOT_FOUND";
            case HttpStatus.FORBIDDEN -> "ALBUM_ACCESS_DENIED";
            case HttpStatus.BAD_REQUEST -> "ALBUM_REQUEST_INVALID";
            default -> "ALBUM_ERROR";
        };
        return ResponseEntity.status(e.getStatusCode())
                .body(new BaseApiResponse<>(code, e.getReason(), null));
    }
}