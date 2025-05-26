package ongi.ongibe.global.exception.handler;

import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.global.exception.InvalidTokenException;
import ongi.ongibe.global.exception.TokenNotFoundException;
import ongi.ongibe.global.exception.TokenParsingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class TokenExceptionHandler {

    @ExceptionHandler(TokenParsingException.class)
    public ResponseEntity<BaseApiResponse<Void>> handleTokenParsingException(TokenParsingException e) {
        log.warn("토큰 파싱 에러 : {}", e.getMessage());

        Sentry.withScope(scope -> {
            scope.setTag("token.type", "kakao");
            scope.setExtra("exceptionMessage", e.getMessage());
            Sentry.captureMessage("KAKAO_TOKEN_ERROR: 토큰 파싱 실패");
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new BaseApiResponse<>("KAKAO_TOKEN_ERROR", e.getMessage(), null));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<BaseApiResponse<Void>> handleInvalidTokenException(InvalidTokenException e) {
        log.warn("토큰 검증 에러 : {}", e.getMessage());

//        Sentry.withScope(scope -> {
//            scope.setTag("token.valid", "false");
//            scope.setExtra("exceptionMessage", e.getMessage());
//            Sentry.captureMessage("TOKEN_INVALID: 토큰 검증 실패");
//        });

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new BaseApiResponse<>("TOKEN_INVALID", e.getMessage(), null));
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<BaseApiResponse<Void>> handleTokenNotFoundException(TokenNotFoundException e) {
        log.warn("토큰을 찾을 수 없음 : {}", e.getMessage());

//        Sentry.withScope(scope -> {
//            scope.setTag("token.exists", "false");
//            scope.setExtra("exceptionMessage", e.getMessage());
//            Sentry.captureMessage("TOKEN_NOT_FOUND: 토큰 없음");
//        });

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new BaseApiResponse<>("TOKEN_NOT_FOUND", e.getMessage(), null));
    }
}
