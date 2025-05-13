package ongi.ongibe.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SecurityUtilException extends ResponseStatusException {

    public SecurityUtilException(HttpStatus status, String message) {
        super(status, message);
    }
}
