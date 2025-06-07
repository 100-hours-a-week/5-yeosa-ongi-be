package ongi.ongibe.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.auth.dto.RefreshAccessTokenRequestDTO;
import ongi.ongibe.domain.auth.dto.RefreshAccessTokenResponseDTO;
import ongi.ongibe.domain.auth.dto.RefreshTokenRequestDTO;
import ongi.ongibe.domain.auth.service.AuthService;
import ongi.ongibe.global.exception.InvalidTokenException;
import ongi.ongibe.global.exception.TokenNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "인증 관련 API", description = "인증에 필요한 API 관련 문서입니다.")
@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @Value("${spring.kakao.auth.client}")
    private String client;

    @Value("${spring.kakao.auth.redirect}")
    private String redirect;

    private final AuthService authService;
    private static final String KAKAO_AUTH_BASE_URL = "https://kauth.kakao.com/oauth/authorize";

    @Operation(summary = "카카오 로그인 리다이렉트", description = "카카오 로그인 화면으로 리다이렉트합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "카카오 로그인 페이지로 리다이렉트 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류로 인한 리다이렉트 실패")
    })
    @GetMapping
    public void redirectToKakaoLogin(HttpServletResponse response) {
        try {
            String redirectUrl = KAKAO_AUTH_BASE_URL
                    + "?response_type=code"
                    + "&client_id=" + client
                    + "&redirect_uri=" + redirect;

            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Redirect 실패", e);
        }
    }

    @Operation(summary = "Access Token 재발급", description = "Refresh Token을 이용하여 새로운 Access Token을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access Token 재발급 성공"),
            @ApiResponse(responseCode = "400", description = "Refresh Token 누락 또는 유효하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/refresh")
    public ResponseEntity<BaseApiResponse<RefreshAccessTokenResponseDTO>> refreshAccessToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh Token 요청 DTO",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RefreshAccessTokenRequestDTO.class))
            )
            @RequestBody RefreshAccessTokenRequestDTO request) {
        log.info("리프레시 토큰 요청 도달: {}", request.refreshToken());
        BaseApiResponse<RefreshAccessTokenResponseDTO> response = authService.reissueAccessToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그아웃", description = "Access Token과 Refresh Token을 기반으로 로그아웃을 수행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "Refresh Token 누락"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ResponseEntity<BaseApiResponse<Void>> logout(
            @Parameter(description = "Access Token", required = true)
            @RequestHeader("Authorization") String authorizationHeader,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh Token 요청 DTO",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RefreshTokenRequestDTO.class))
            )
            @RequestBody RefreshTokenRequestDTO refreshTokenRequest) {

        if (refreshTokenRequest.refreshToken() == null) {
            throw new TokenNotFoundException("refresh token이 없습니다.");
        }

        authService.logout(authorizationHeader, refreshTokenRequest.refreshToken());
        return ResponseEntity.ok(BaseApiResponse.success("LOGOUT_SUCCESS", "로그아웃이 완료되었습니다.", null));
    }
}
