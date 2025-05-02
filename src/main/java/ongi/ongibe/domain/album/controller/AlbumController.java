package ongi.ongibe.domain.album.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumCreateRequestDTO;
import ongi.ongibe.domain.album.dto.AlbumDetailResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumSummaryResponseDTO;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO;
import ongi.ongibe.domain.album.service.AlbumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "앨범 API", description = "앨범 관련 API 문서입니다")
@RestController
@RequestMapping("/api/album")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @Operation(summary = "월별 앨범 목록 조회", description = "요청한 연월에 등록된 앨범 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "앨범 조회 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/monthly")
    public ResponseEntity<BaseApiResponse<MonthlyAlbumResponseDTO>> getMonthlyAlbum(@RequestParam(required = false) String yearMonth) {
        BaseApiResponse<MonthlyAlbumResponseDTO> response = albumService.getMonthlyAlbum(yearMonth);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "앨범 요약 조회", description = "앨범 ID 기반으로 요약 정보 조회")
    @GetMapping("/{albumId}/summary")
    public ResponseEntity<BaseApiResponse<List<AlbumSummaryResponseDTO>>> getAlbumSummary(@PathVariable Long albumId) {
        BaseApiResponse<List<AlbumSummaryResponseDTO>> response = albumService.getAlbumSummary(albumId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "앨범 상세 조회", description = "앨범 ID 기반으로 상세 정보 조회")
    @GetMapping("/{albumId}")
    public ResponseEntity<BaseApiResponse<AlbumDetailResponseDTO>> getAlbumDetail(@PathVariable Long albumId) {
        BaseApiResponse<AlbumDetailResponseDTO> response = albumService.getAlbumDetail(albumId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<BaseApiResponse<Void>> createAlbum(@RequestBody AlbumCreateRequestDTO request) {

    }
}
