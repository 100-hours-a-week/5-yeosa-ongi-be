package ongi.ongibe.domain.album.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.album.dto.*;
import ongi.ongibe.domain.album.service.AlbumService;
import ongi.ongibe.swagger.album.BaseApiResponse_AlbumDetailResponse;
import ongi.ongibe.swagger.album.BaseApiResponse_AlbumInviteResponse;
import ongi.ongibe.swagger.album.BaseApiResponse_AlbumMemberResponse;
import ongi.ongibe.swagger.album.BaseApiResponse_AlbumOwnerTransferResponse;
import ongi.ongibe.swagger.album.BaseApiResponse_AlbumSummaryResponseList;
import ongi.ongibe.swagger.album.BaseApiResponse_MonthlyAlbumResponse;
import ongi.ongibe.swagger.album.BaseApiResponse_String;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "앨범 API", description = "앨범 관련 API 문서입니다")
@RestController
@RequestMapping("/api/album")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @Operation(summary = "월별 앨범 목록 조회", description = "요청한 연월에 등록된 앨범 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "앨범 조회 성공", content = @Content(schema = @Schema(implementation = BaseApiResponse_MonthlyAlbumResponse.class)))
    @GetMapping("/monthly")
    public ResponseEntity<BaseApiResponse<MonthlyAlbumResponseDTO>> getMonthlyAlbum(@RequestParam(required = false) String yearMonth) {
        return ResponseEntity.ok(albumService.getMonthlyAlbum(yearMonth));
    }

    @Operation(summary = "앨범 요약 조회", description = "앨범 ID 기반으로 요약 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "앨범 요약 조회 성공", content = @Content(schema = @Schema(implementation = BaseApiResponse_AlbumSummaryResponseList.class)))
    @GetMapping("/{albumId}/summary")
    public ResponseEntity<BaseApiResponse<List<AlbumSummaryResponseDTO>>> getAlbumSummary(@PathVariable Long albumId) {
        return ResponseEntity.ok(albumService.getAlbumSummary(albumId));
    }

    @Operation(summary = "앨범 상세 조회", description = "앨범 ID로 상세 정보 조회")
    @ApiResponse(responseCode = "200", description = "앨범 상세 조회 성공", content = @Content(schema = @Schema(implementation = BaseApiResponse_AlbumDetailResponse.class)))
    @GetMapping("/{albumId}")
    public ResponseEntity<BaseApiResponse<AlbumDetailResponseDTO>> getAlbumDetail(@PathVariable Long albumId) {
        return ResponseEntity.ok(albumService.getAlbumDetail(albumId));
    }

    @Operation(summary = "앨범 생성", description = "앨범 이름과 사진 URL을 기반으로 앨범을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "앨범 생성 요청 접수 성공")
    @PostMapping
    public ResponseEntity<BaseApiResponse<Void>> createAlbum(@RequestBody AlbumCreateRequestGeoFrontDTO request) {
        List<? extends PictureUrlCoordinateDTO> pictureDTOs = request.pictureUrls(); // 명시적 타입
        albumService.createAlbum(request.albumName(), pictureDTOs);
        return ResponseEntity.ok(BaseApiResponse.success("ALBUM_CREATE_SUCCESS", "앨범 생성 요청이 접수되었습니다.", null));
    }
//    public ResponseEntity<BaseApiResponse<Void>> createAlbum(@RequestBody AlbumCreateRequestDTO request) {
//        albumService.createAlbum(request.albumName(), request.pictureUrls());
//        return ResponseEntity.ok(BaseApiResponse.success("ALBUM_CREATE_SUCCESS", "앨범 생성 요청이 접수되었습니다.", null));
//    }

    @Operation(summary = "앨범 사진 추가", description = "기존 앨범에 사진을 추가합니다.")
    @ApiResponse(responseCode = "200", description = "사진 추가 성공")
    @PostMapping("/{albumId}")
    public ResponseEntity<BaseApiResponse<Void>> addPictureAlbum(@PathVariable Long albumId, @RequestBody AlbumPictureAddRequestGeoFrontDTO request) {
        List<? extends PictureUrlCoordinateDTO> pictureDTOs = request.pictureUrls();
        albumService.addPictures(albumId, pictureDTOs);
        return ResponseEntity.ok(BaseApiResponse.success("PICTURE_ADD_SUCCESS", "앨범 사진 추가 요청이 접수되었습니다.", null));
    }
//    public ResponseEntity<BaseApiResponse<Void>> addPictureAlbum(@PathVariable Long albumId, @RequestBody AlbumPictureAddRequestDTO request) {
//        albumService.addPictures(albumId, request.pictureUrls());
//        return ResponseEntity.ok(BaseApiResponse.success("PICTURE_ADD_SUCCESS", "앨범 사진 추가 요청이 접수되었습니다.", null));
//    }

    @Operation(summary = "앨범 이름 수정", description = "앨범의 이름을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "앨범 이름 수정 성공")
    @PutMapping("/{albumId}")
    public ResponseEntity<BaseApiResponse<Void>> updateAlbumTitle(@PathVariable Long albumId, @RequestBody AlbumNameUpdateRequestDTO request) {
        albumService.updateAlbumName(albumId, request.albumName());
        return ResponseEntity.ok(BaseApiResponse.success("ALBUM_NAME_UPDATE_SUCCESS", "앨범 이름을 수정했습니다.", null));
    }

    @Operation(summary = "앨범 사진 복원", description = "삭제된 사진을 복원 처리합니다.")
    @ApiResponse(responseCode = "200", description = "사진 복원 성공")
    @PutMapping("/{albumId}/picture")
    public ResponseEntity<BaseApiResponse<Void>> updatePictureState(@PathVariable Long albumId, @RequestBody AlbumPictureUpdateRequestDTO request) {
        albumService.updatePicture(albumId, request.pictureIds());
        return ResponseEntity.ok(BaseApiResponse.success("PICTURE_RESTORE_SUCCESS", "앨범 내 사진을 복원했습니다.", null));
    }

    @Operation(summary = "앨범 사진 삭제", description = "앨범 내에서 선택된 사진을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "사진 삭제 성공")
    @DeleteMapping("/{albumId}/picture")
    public ResponseEntity<BaseApiResponse<Void>> deletePicture(@PathVariable Long albumId, @RequestBody AlbumPictureUpdateRequestDTO request) {
        albumService.deletePictures(albumId, request.pictureIds());
        return ResponseEntity.ok(BaseApiResponse.success("PICTURE_DELETE_SUCCESS", "앨범 내 사진을 삭제했습니다.", null));
    }

    @Operation(summary = "앨범 삭제", description = "앨범 전체를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "앨범 삭제 성공")
    @DeleteMapping("/{albumId}")
    public ResponseEntity<BaseApiResponse<Void>> deleteAlbum(@PathVariable Long albumId) {
        albumService.deleteAlbum(albumId);
        return ResponseEntity.ok(BaseApiResponse.success("ALBUM_DELETE_SUCCESS", "앨범이 삭제되었습니다.", null));
    }

    @Operation(summary = "앨범 멤버 목록 조회", description = "앨범에 초대된 멤버들의 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "앨범 멤버 조회 성공", content = @Content(schema = @Schema(implementation = BaseApiResponse_AlbumMemberResponse.class)))
    @GetMapping("/{albumId}/members")
    public ResponseEntity<BaseApiResponse<AlbumMemberResponseDTO>> getAlbumMembers(@PathVariable Long albumId) {
        return ResponseEntity.ok(albumService.getAlbumMembers(albumId));
    }

    @Operation(summary = "앨범 초대링크 생성", description = "해당 앨범의 초대링크를 생성합니다.")
    @ApiResponse(responseCode = "200", description = "초대 링크 생성 성공", content = @Content(schema = @Schema(implementation = BaseApiResponse_String.class)))
    @PostMapping("/{albumId}/invite/link")
    public ResponseEntity<BaseApiResponse<String>> createInviteLink(@PathVariable Long albumId) {
        return ResponseEntity.ok(albumService.createInviteToken(albumId));
    }

    @Operation(summary = "앨범 초대 수락", description = "초대 링크를 통해 앨범에 참여합니다.")
    @ApiResponse(responseCode = "200", description = "초대 수락 성공", content = @Content(schema = @Schema(implementation = BaseApiResponse_AlbumInviteResponse.class)))
    @PostMapping("/invite")
    public ResponseEntity<BaseApiResponse<AlbumInviteResponseDTO>> acceptInvite(@RequestParam String inviteToken) {
        return ResponseEntity.ok(albumService.acceptInvite(inviteToken));
    }

    @Operation(summary = "앨범 소유권 이전", description = "해당 앨범의 소유권을 다른 유저에게 이전합니다.")
    @ApiResponse(responseCode = "200", description = "소유권 이전 성공", content = @Content(schema = @Schema(implementation = BaseApiResponse_AlbumOwnerTransferResponse.class)))
    @PostMapping("/{albumId}/owner")
    public ResponseEntity<BaseApiResponse<AlbumOwnerTransferResponseDTO>> transferOwner(@PathVariable Long albumId, @RequestBody Long newOwnerId) {
        return ResponseEntity.ok(albumService.transferAlbumOwner(albumId, newOwnerId));
    }
}
