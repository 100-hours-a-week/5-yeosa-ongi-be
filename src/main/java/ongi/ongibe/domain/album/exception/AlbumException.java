package ongi.ongibe.domain.album.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AlbumException extends ResponseStatusException {

    public AlbumException(HttpStatus status, String message) {
        super(status, message);
        System.out.println("AlbumException!!!!!!!!!!!!!!!!");
    }
}
