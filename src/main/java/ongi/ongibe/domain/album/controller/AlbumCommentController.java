package ongi.ongibe.domain.album.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumCommentRequestDTO;
import ongi.ongibe.domain.album.dto.AlbumCommentResponseDTO;
import ongi.ongibe.domain.album.entity.Comments;
import ongi.ongibe.domain.album.service.AlbumCommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "앨범 API", description = "앨범 관련 API 문서입니다")
@RestController
@RequestMapping("/api/album/{albumId}")
@RequiredArgsConstructor
public class AlbumCommentController {

    private final AlbumCommentService albumCommentService;

    @GetMapping("/comments")
    public ResponseEntity<BaseApiResponse<List<AlbumCommentResponseDTO>>> findAllByAlbumId(@PathVariable Long albumId) {
        return ResponseEntity.ok(albumCommentService.readComments(albumId));
    }

    @PostMapping("/comments")
    public ResponseEntity<BaseApiResponse<Void>> createComments(@PathVariable Long albumId, @RequestBody
            AlbumCommentRequestDTO request) {
        albumCommentService.createComments(albumId, request.comments());
        return ResponseEntity.ok(BaseApiResponse.success("COMMENT_CREATE_SUCCESS", "댓글 작성이 성공했습니다.", null));
    }

    @PutMapping("/comments/{commentsId}")
    public ResponseEntity<BaseApiResponse<Void>> updateComments(@PathVariable Long albumId, @PathVariable Long commentsId, @RequestBody AlbumCommentRequestDTO request) {
        albumCommentService.updateComments(albumId, commentsId, request.comments());
        return ResponseEntity.ok(BaseApiResponse.success("COMMENT_UPDATE_SUCCESS", "댓글 수정이 성공했습니다.", null));
    }
}
