package ongi.ongibe.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes(StandardCharsets.UTF_8));
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }


    private static final long ACCESS_TOKEN_VALIDITY = 60 * 5L;
    private static final long REFRESH_TOKEN_VALIDITY = 14 * 24 * 60 * 60L;
    private static final long INVITE_TOKEN_VALIDITY = 24 * 60 * 60L;

    public String generateAccessToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .expiration(Date.from(Instant.now().plusSeconds(ACCESS_TOKEN_VALIDITY)))
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .expiration(Date.from(Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY)))
                .signWith(signingKey)
                .compact();
    }

    public Long validateAndExtractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.parseLong(claims.getSubject());

        } catch (ExpiredJwtException e) {
            log.warn("토큰 만료: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다.");

        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 JWT: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT 토큰이 잘못되었습니다.");

        } catch (SecurityException e) {
            log.warn("JWT 서명 검증 실패: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 서명입니다.");

        } catch (IllegalArgumentException e) {
            log.warn("JWT 파싱 시 잘못된 인자: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT 파싱에 실패했습니다.");
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("토큰 유효성 검사 실패: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "토큰이 유효하지 않습니다.");
        }
    }

    public String generateInviteToken(Long albumId){
        return Jwts.builder()
                .subject(String.valueOf(albumId))
                .expiration(Date.from(Instant.now().plusSeconds(INVITE_TOKEN_VALIDITY)))
                .signWith(signingKey)
                .compact();
    }

    public Long validateAndExtractInviteId(String token){
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            log.warn("토큰 유효성 검사 실패: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "토큰이 유효하지 않습니다.");
        }
    }
}

