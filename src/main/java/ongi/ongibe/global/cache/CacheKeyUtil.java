package ongi.ongibe.global.cache;

public class CacheKeyUtil {

    private static final String PREFIX = "cache";

    public static String key(String namespace, Object... parts) {
        StringBuilder sb = new StringBuilder(PREFIX).append("::").append(namespace);
        for (Object part : parts) {
            sb.append("::").append(part);
        }
        return sb.toString();
    }
}
