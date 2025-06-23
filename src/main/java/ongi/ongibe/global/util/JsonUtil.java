package ongi.ongibe.global.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJson(List<String> s3keys) {
        try {
            return mapper.writeValueAsString(s3keys);
        } catch (Exception e) {
            throw new RuntimeException("직렬화 실패", e);
        }
    }

    public static List<String> fromJson(String json) {
        try {
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("역직렬화 실패", e);
        }
    }
}
