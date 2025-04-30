package ongi.ongibe.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import ongi.ongibe.common.ApiResponse;
import ongi.ongibe.domain.auth.dto.RefreshAccessTokenRequestDTO;
import ongi.ongibe.domain.auth.dto.RefreshAccessTokenResponseDTO;
import ongi.ongibe.domain.auth.dto.RefreshTokenRequestDTO;
import ongi.ongibe.domain.auth.service.AuthService;
import ongi.ongibe.global.exception.InvalidRequestException;
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

@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${spring.kakao.auth.client}")
    private String client;

    @Value("${spring.kakao.auth.redirect}")
    private String redirect;

    private final AuthService authService;
    private static final String KAKAO_AUTH_BASE_URL = "https://kauth.kakao.com/oauth/authorize";

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

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshAccessTokenResponseDTO>> refreshAccessToken(@RequestBody RefreshAccessTokenRequestDTO request) {
        ApiResponse<RefreshAccessTokenResponseDTO> response = authService.reissueAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody RefreshTokenRequestDTO refreshTokenRequest) {

        if (refreshTokenRequest.getRefreshToken() == null) {
            throw new InvalidRequestException("refresh token이 누락되었습니다.");
        }

        authService.logout(authorizationHeader, refreshTokenRequest.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("LOGOUT_SUCCESS", "로그아웃이 완료되었습니다.", null));
    }
}