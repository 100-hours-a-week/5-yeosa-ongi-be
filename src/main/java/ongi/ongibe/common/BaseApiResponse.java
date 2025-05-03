package ongi.ongibe.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BaseApiResponse<T> {
    private String code;
    private String message;
    private T data;

    public static <T> BaseApiResponse<T> success(String code, String message, T data) {
        return BaseApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }
}
