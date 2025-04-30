package ongi.ongibe.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.common.BaseApiResponse;
import ongi.ongibe.domain.auth.dto.KakaoLoginResponseDTO;
import ongi.ongibe.domain.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class KakaoController {

    private final AuthService authService;

    @GetMapping("/auth/login/kakao")
    public ResponseEntity<BaseApiResponse<KakaoLoginResponseDTO>> kakaoLogin(@RequestParam("code") String code) {
        BaseApiResponse<KakaoLoginResponseDTO> response = authService.kakaoLogin(code);
        return ResponseEntity.ok(response);
    }
}
