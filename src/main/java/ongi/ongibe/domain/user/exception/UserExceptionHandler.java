package ongi.ongibe.domain.user.exception;

import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.common.BaseApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class UserExceptionHandler {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<BaseApiResponse<Void>> userExceptionHandler(UserException e){
        log.warn("유저 검증 오류 : {}", e.getReason());
        String message = switch(e.getStatusCode()){
            case HttpStatus.BAD_REQUEST -> "INVALID_USER_INPUT";
            default -> "UNKNOWN_ERROR";
        };
        return ResponseEntity.status(e.getStatusCode())
                .body(new BaseApiResponse<>(message, e.getReason(), null));
    }

}
