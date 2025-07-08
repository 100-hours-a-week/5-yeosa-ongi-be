package ongi.ongibe.domain.ai.aiInterface;

import java.util.List;
import ongi.ongibe.domain.album.entity.Album;

public interface AiAlbumServiceInterface {

    void process(Album album, Long userId, List<String> s3keys, List<String> concepts);

}
