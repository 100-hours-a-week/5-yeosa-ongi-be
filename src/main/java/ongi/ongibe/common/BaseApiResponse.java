package ongi.ongibe.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "success")
@Builder
public class BaseApiResponse<T> {
    private String code;
    private String message;
    private T data;
}
