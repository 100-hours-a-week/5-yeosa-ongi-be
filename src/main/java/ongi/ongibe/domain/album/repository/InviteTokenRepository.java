package ongi.ongibe.domain.album.repository;

public interface InviteTokenRepository {
    void save(String token, Long albumId);
    Long getAlbumId(String token);
    void remove(String token);
    boolean existsByToken(String token);
}
