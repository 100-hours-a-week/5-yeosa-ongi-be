package ongi.ongibe.global.exception.handler;

import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.global.exception.SecurityUtilException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class SecurityUtilExceptionHandler {

    @ExceptionHandler(SecurityUtilException.class)
    public ResponseEntity<BaseApiResponse<Void>> handleSecurityUtilException(SecurityUtilException e) {
        Sentry.captureException(e);
        log.warn("인증정보 예외 발생 : {}", e.getMessage());
        String code = switch (e.getStatusCode()){
            case HttpStatus.NOT_FOUND -> "USER_NOT_FOUND";
            case HttpStatus.UNAUTHORIZED -> "NO_AUTH_INFO";
            default -> "UNKNOWN_ERROR";
        };
        return ResponseEntity
                .status(e.getStatusCode())
                .body(
                        new BaseApiResponse<>(code, e.getReason(), null)
                );
    }

}
