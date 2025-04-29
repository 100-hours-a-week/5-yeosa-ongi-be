package ongi.ongibe.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "success")
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;
}
