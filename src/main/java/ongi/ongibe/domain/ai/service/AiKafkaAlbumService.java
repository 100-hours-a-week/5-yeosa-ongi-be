package ongi.ongibe.domain.ai.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.domain.album.entity.Album;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Qualifier("kafka")
@Service
@RequiredArgsConstructor
@Slf4j
public class AiKafkaAlbumService implements AiAlbumServiceInterface {

    @Override
    public void process(Album album, List<String> s3keys) {

    }
}
