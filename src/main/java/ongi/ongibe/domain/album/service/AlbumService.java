package ongi.ongibe.domain.album.service;

import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;

}
