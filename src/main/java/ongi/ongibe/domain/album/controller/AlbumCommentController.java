package ongi.ongibe.domain.album.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.domain.album.service.AlbumCommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "앨범 API", description = "앨범 관련 API 문서입니다")
@RestController
@RequestMapping("/api/album/{albumId}")
@RequiredArgsConstructor
public class AlbumCommentController {

    private final AlbumCommentService albumCommentService;

    @GetMapping("/comments")
    public ResponseEntity<>

}
