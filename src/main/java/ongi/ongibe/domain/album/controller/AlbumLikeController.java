package ongi.ongibe.domain.album.controller;

import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumLikeResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumLikeToggleResponseDTO;
import ongi.ongibe.domain.album.service.AlbumLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<BaseApiResponse<AlbumLikeToggleResponseDTO>> likeToggleAlbum(@PathVariable Long albumId){
        BaseApiResponse<AlbumLikeToggleResponseDTO> response = albumLikeService.albumLikeToggle(albumId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<BaseApiResponse<AlbumLikeResponseDTO>> getAlbumLike(@PathVariable Long albumId){
        BaseApiResponse<AlbumLikeResponseDTO> response = albumLikeService.getAlbumLike(albumId);
        return ResponseEntity.ok(response);
    }

}
