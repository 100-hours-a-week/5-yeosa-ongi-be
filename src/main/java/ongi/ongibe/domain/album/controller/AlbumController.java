package ongi.ongibe.domain.album.controller;

import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.ApiResponse;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO;
import ongi.ongibe.domain.album.service.AlbumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/album")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<MonthlyAlbumResponseDTO>> getMonthlyAlbum(@RequestParam String yearMonth) {
        ApiResponse<MonthlyAlbumResponseDTO> response = albumService.getMonthlyAlbum(yearMonth);
        return ResponseEntity.ok(response);
    }
}
