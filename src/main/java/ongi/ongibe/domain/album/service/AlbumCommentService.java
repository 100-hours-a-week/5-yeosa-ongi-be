package ongi.ongibe.domain.album.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumCommentResponseDTO;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Comments;
import ongi.ongibe.domain.album.exception.AlbumException;
import ongi.ongibe.domain.album.repository.AlbumRepository;
import ongi.ongibe.domain.album.repository.CommentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

@Service
@RequiredArgsConstructor
public class AlbumCommentService {

    private final CommentRepository commentRepository;
    private final AlbumRepository albumRepository;

    @Transactional(readOnly = true)
    public BaseApiResponse<List<AlbumCommentResponseDTO>> readComments(@PathVariable("albumId") Long albumId) {
        Album album = albumRepository.findById(albumId).orElseThrow(
                () -> new AlbumException(HttpStatus.NOT_FOUND, "앨범을 찾을 수 없습니다.")
        );
        List<Comments> comments = commentRepository.findAllByAlbum(album);
        List<AlbumCommentResponseDTO> dtoList = comments.stream()
                .map(AlbumCommentResponseDTO::from).toList();
        return BaseApiResponse.success("COMMENT_READ_SUCCESS", "댓글 조회에 성공했습니다.",  dtoList);
    }
}
