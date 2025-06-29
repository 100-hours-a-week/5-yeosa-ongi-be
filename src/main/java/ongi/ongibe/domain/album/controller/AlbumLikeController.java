package ongi.ongibe.domain.album.controller;

import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumLikeResponseDTO;
import ongi.ongibe.domain.album.service.AlbumLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/album/{albumId}/like")
public class AlbumLikeController {

    private final AlbumLikeService albumLikeService;

    @PostMapping
    public ResponseEntity<BaseApiResponse<AlbumLikeResponseDTO>> likeToggleAlbum(@PathVariable Long albumId){
        BaseApiResponse<AlbumLikeResponseDTO> response = albumLikeService.albumLikeToggle(albumId);
        return ResponseEntity.ok(response);
    }

}
