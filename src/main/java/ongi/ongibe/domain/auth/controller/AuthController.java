package ongi.ongibe.domain.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${kakao.auth.client}")
    private String clientId;

    @Value("${kakao.auth.redirect}")
    private String redirectURI;

    private static final String KAKAO_AUTH_BASE_URL = "https://kauth.kakao.com/oauth/authorize";

    @GetMapping
    public void redirectToKakaoLogin(HttpServletResponse response) {
        try {
            String redirectUrl = KAKAO_AUTH_BASE_URL
                    + "?response_type=code"
                    + "&client_id=" + clientId
                    + "&redirect_uri=" + redirectURI;

            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Redirect 실패", e);
        }
    }
}