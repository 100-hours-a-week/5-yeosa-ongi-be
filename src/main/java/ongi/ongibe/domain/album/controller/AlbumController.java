package ongi.ongibe.domain.album.controller;

import com.fasterxml.jackson.databind.ser.Serializers.Base;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.AlbumCreateRequestDTO;
import ongi.ongibe.domain.album.dto.AlbumDetailResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumInviteResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumMemberResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumNameUpdateRequestDTO;
import ongi.ongibe.domain.album.dto.AlbumOwnerTransferResponseDTO;
import ongi.ongibe.domain.album.dto.AlbumPictureAddRequestDTO;
import ongi.ongibe.domain.album.dto.AlbumPictureUpdateRequestDTO;
import ongi.ongibe.domain.album.dto.AlbumSummaryResponseDTO;
import ongi.ongibe.domain.album.dto.MonthlyAlbumResponseDTO;
import ongi.ongibe.domain.album.entity.Album;
import ongi.ongibe.domain.album.entity.Picture;
import ongi.ongibe.domain.album.service.AlbumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @Operation(summary = "앨범 생성", description = "사진을 추가하면 앨범 생성됨.")
    @PostMapping
    public ResponseEntity<BaseApiResponse<Void>> createAlbum(@RequestBody AlbumCreateRequestDTO request) {
        Album album = albumService.createAlbum(request.albumName(), request.pictureUrls());
        BaseApiResponse<Void> response =  BaseApiResponse.success("ALBUM_CREATE_SUCCESS", "앨범 생성 요청이 접수되었습니다.", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{albumId}")
    public ResponseEntity<BaseApiResponse<Void>> createAlbum(@PathVariable Long albumId, @RequestBody AlbumPictureAddRequestDTO request) {
        Album album = albumService.addPictures(albumId, request.pictureUrls());
        BaseApiResponse<Void> response = BaseApiResponse.success("PICTURE_ADD_SUCCESS", "앨범 사진 추가 요청이 접수되었습니다.", null);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{albumId}")
    public ResponseEntity<BaseApiResponse<Void>> updateAlbumTitle(@PathVariable Long albumId, @RequestBody AlbumNameUpdateRequestDTO request) {
        Album album = albumService.updateAlbumName(albumId, request.albumName());
        BaseApiResponse<Void> response = BaseApiResponse.success("ALBUMNAEM_UPDATE_SUCCESS", "앨범 이름을 수정했습니다.", null);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{albumId}/picture")
    public ResponseEntity<BaseApiResponse<Void>> updatePictureState(@PathVariable Long albumId, @RequestBody AlbumPictureUpdateRequestDTO request) {
        List<Picture> pictures = albumService.updatePicture(albumId, request.pictureIds());
        BaseApiResponse<Void> response = BaseApiResponse.success("PICTURE_RESTORE_SUCCESS", "앨범 내 사진을 복원했습니다.", null);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{albumId}/picture")
    public ResponseEntity<BaseApiResponse<Void>> deletePicture(@PathVariable Long albumId, @RequestBody AlbumPictureUpdateRequestDTO request) {
        albumService.deletePictures(albumId, request.pictureIds());
        BaseApiResponse<Void> response = BaseApiResponse.success("PICTURE_DELTE_SUCCESS", "앨범 내 사진을 삭제했습니다.", null);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<BaseApiResponse<Void>> deleteAlbum(@PathVariable Long albumId) {
        albumService.deleteAlbum(albumId);
        BaseApiResponse<Void> response = BaseApiResponse.success("ALBUM_DELETE_SUCCESS", "앨범이 삭제되었습니다.", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{albumId}/members")
    public ResponseEntity<BaseApiResponse<AlbumMemberResponseDTO>> getAlbumMembers(@PathVariable Long albumId) {
        BaseApiResponse<AlbumMemberResponseDTO> response = albumService.getAlbumMembers(albumId);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{albumId}/invite/link")
    public ResponseEntity<BaseApiResponse<String>> createInviteLink(@PathVariable Long albumId) {
        BaseApiResponse<String> response = albumService.createInviteToken(albumId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invite")
    public ResponseEntity<BaseApiResponse<AlbumInviteResponseDTO>> acceptInvite(@RequestBody String inviteToken) {
        BaseApiResponse<AlbumInviteResponseDTO> response = albumService.acceptInvite(inviteToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{albumId}/owner")
    public ResponseEntity<BaseApiResponse<AlbumOwnerTransferResponseDTO>> transferOwner(@PathVariable Long albumId, @RequestBody Long newOwnerId) {
        BaseApiResponse<AlbumOwnerTransferResponseDTO> response = albumService.transferAlbumOwner(albumId, newOwnerId);
        return ResponseEntity.ok(response);
    }
}
