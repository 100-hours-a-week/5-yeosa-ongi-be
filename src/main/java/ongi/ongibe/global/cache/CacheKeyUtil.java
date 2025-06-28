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

    public static String albumLikeCountKey(Long albumId) {
        return PREFIX + "album:%d:like_count".formatted(albumId);
    }

    public static String albumUserLikedKey(Long albumId, Long userId) {
        return PREFIX + "album:%d:liked:%d".formatted(albumId, userId);
    }
}
