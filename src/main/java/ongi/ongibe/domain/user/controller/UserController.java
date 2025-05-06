package ongi.ongibe.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.user.dto.UserPictureStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserPlaceStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserTagStatResponseDTO;
import ongi.ongibe.domain.user.dto.UserTotalStateResponseDTO;
import ongi.ongibe.domain.user.service.UserService;
import ongi.ongibe.swagger.user.BaseApiResponse_UserPictureStatResponse;
import ongi.ongibe.swagger.user.BaseApiResponse_UserPlaceStatResponse;
import ongi.ongibe.swagger.user.BaseApiResponse_UserTagStatResponse;
import ongi.ongibe.swagger.user.BaseApiResponse_UserTotalStateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "유저 API", description = "유저 관련 API 문서입니다")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "유저 전체 통계 조회", description = "유저가 올린 사진의 위치, 앨범 수, 장소 수를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "유저 통계 조회 성공", content = @Content(schema = @Schema(implementation = BaseApiResponse_UserTotalStateResponse.class)))
    @GetMapping("/statistics")
    public ResponseEntity<BaseApiResponse<UserTotalStateResponseDTO>> getUserTotalState() {
        BaseApiResponse<UserTotalStateResponseDTO> response = userService.getUserTotalState();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "월별 사진 업로드 수 조회", description = "해당 연월에 유저가 업로드한 사진의 일별 개수를 조회합니다.")
    @Parameters({
            @Parameter(name = "yearMonth", description = "연월 (예: 2025-04)", required = true)
    })
    @ApiResponse(responseCode = "200", description = "월간 일별 사진 업로드 수 조회 성공", content = @Content(schema = @Schema(implementation = BaseApiResponse_UserPictureStatResponse.class)))
    @GetMapping("/statistics/picture")
    public ResponseEntity<BaseApiResponse<UserPictureStatResponseDTO>> getUsePictureStat(
            @RequestParam String yearMonth) {
        BaseApiResponse<UserPictureStatResponseDTO> response = userService.getUserPictureStat(yearMonth);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "가장 많이 방문한 장소 조회", description = "해당 연월에 유저가 가장 많이 방문한 장소와 그 장소에서 찍은 상위 태그를 조회합니다.")
    @Parameters({
            @Parameter(name = "yearMonth", description = "연월 (예: 2025-04)", required = true)
    })
    @ApiResponse(responseCode = "200", description = "유저 방문 장소 조회 성공", content = @Content(schema = @Schema(implementation = BaseApiResponse_UserPlaceStatResponse.class)))
    @GetMapping("/statistics/place")
    public ResponseEntity<BaseApiResponse<UserPlaceStatResponseDTO>> getUserPlaceStat(
            @RequestParam String yearMonth) {
        BaseApiResponse<UserPlaceStatResponseDTO> response = userService.getUserPlaceStat(yearMonth);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "월별 유저 최다 태그 및 관련 사진 조회", description = "해당 연월에 유저가 가장 많이 기록한 태그와 해당 태그가 붙은 상위 4개 사진을 조회합니다.")
    @Parameters({
            @Parameter(name = "yearMonth", description = "연월 (예: 2025-04)", required = true)
    })
    @ApiResponse(responseCode = "200", description = "월별 유저 최다기록 태그 및 사진 조회 성공", content = @Content(schema = @Schema(implementation = BaseApiResponse_UserTagStatResponse.class)))
    @GetMapping("/statistics/tag")
    public ResponseEntity<BaseApiResponse<UserTagStatResponseDTO>> getUserTagStat(
            @RequestParam String yearMonth) {
        BaseApiResponse<UserTagStatResponseDTO> response = userService.getUserTagStat(yearMonth);
        return ResponseEntity.ok(response);
    }
}
