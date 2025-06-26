package ongi.ongibe.domain.album.service;

import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.album.repository.CommentRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlbumCommentService {

    private final CommentRepository commentRepository;



}
