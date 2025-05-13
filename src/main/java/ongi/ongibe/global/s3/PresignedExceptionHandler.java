package ongi.ongibe.global.s3;

import ongi.ongibe.common.BaseApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PresignedExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .badRequest()
                .body(BaseApiResponse.<Void>builder()
                        .code("INVALID_EXTENSION")
                        .message(ex.getMessage())
                        .data(null)
                        .build());
    }
}
