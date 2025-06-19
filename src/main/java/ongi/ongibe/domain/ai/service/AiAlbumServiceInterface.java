package ongi.ongibe.domain.ai.service;

import java.util.List;
import ongi.ongibe.domain.album.entity.Album;

public interface AiAlbumServiceInterface {

    void process(Album album, List<String> s3keys);

}
