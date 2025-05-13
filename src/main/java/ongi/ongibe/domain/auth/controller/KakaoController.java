package ongi.ongibe.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.auth.dto.KakaoLoginResponseDTO;
import ongi.ongibe.domain.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "카카오 인증 API", description = "카카오 인증 후 처리 관련 API 문서입니다.")
@RestController
@RequiredArgsConstructor
@Slf4j
public class KakaoController {

    private final AuthService authService;

    @Operation(summary = "카카오 로그인 완료 처리", description = "카카오 OAuth 인증 후 리다이렉트된 code를 받아 로그인 처리를 수행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 및 access/refresh 토큰 발급"),
            @ApiResponse(responseCode = "400", description = "code 파라미터 누락 또는 인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/auth/login/kakao")
    public ResponseEntity<BaseApiResponse<KakaoLoginResponseDTO>> kakaoLogin(
            @Parameter(description = "카카오에서 전달된 인가 코드", required = true, example = "JzshdfkjHSKJdf8234...")
            @RequestParam("code") String code
    ) {
        BaseApiResponse<KakaoLoginResponseDTO> response = authService.kakaoLogin(code);
        return ResponseEntity.ok(response);
    }
}
