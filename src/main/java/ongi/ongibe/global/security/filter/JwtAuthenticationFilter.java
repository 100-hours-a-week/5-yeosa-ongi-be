package ongi.ongibe.global.security.filter;

import java.io.IOException;

//import io.sentry.Sentry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongi.ongibe.global.security.config.CustomUserDetails;
import ongi.ongibe.util.JwtTokenProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        log.info("요청 경로: {}", path);
        log.info("Authorization 헤더: {}", authHeader);

        if (path.startsWith("/api/auth") || path.startsWith("/sentry/") || path.startsWith("/api/testauth")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try{
                if (jwtTokenProvider.validateToken(token)) {
                    Long userId = jwtTokenProvider.validateAndExtractUserId(token);
                    log.info("userId = {}", userId);
                    log.debug("JwtAuthenticationFilter: 토큰 감지됨 = {}", token);
                    CustomUserDetails userDetails = new CustomUserDetails(userId);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            }catch (ResponseStatusException e) {
                // 에러 로그 기록
                log.warn("JWT 인증 예외 발생: {}", e.getReason());

//                // Sentry에 요약 메시지 + 추가 정보 함께 전송
//                Sentry.withScope(scope -> {
//                    scope.setTag("jwt.error_type", e.getReason().contains("만료") ? "expired" : "invalid");
//                    scope.setTag("request.path", path);
//                    scope.setExtra("authHeader", authHeader);
//                    scope.setExtra("token", authHeader != null ? authHeader.substring(7) : "없음");
//                    scope.setExtra("reason", e.getReason());
//                    Sentry.captureMessage("JWT_ERROR: " + e.getReason());
//                });

                // 응답 JSON 구성
                response.setContentType("application/json");
                response.setStatus(e.getStatusCode().value());

                String message = e.getReason();
                String code = message.contains("만료") ? "ACCESS_TOKEN_EXPIRED" : "INVALID_TOKEN";

                String json = String.format("""
        {
            "code": "%s",
            "message": "%s",
            "data": null
        }
        """, code, message);

                response.getWriter().write(json);
                return;
            }

        }
        filterChain.doFilter(request, response);
    }
}
