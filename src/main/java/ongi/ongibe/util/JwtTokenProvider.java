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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes(StandardCharsets.UTF_8));
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }


    private static final long ACCESS_TOKEN_VALIDITY = 60 * 5L;
    private static final long REFRESH_TOKEN_VALIDITY = 14 * 24 * 60 * 60L;

    public String generateAccessToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(Date.from(Instant.now().plusSeconds(ACCESS_TOKEN_VALIDITY)))
                .signWith(SignatureAlgorithm.HS256, signingKey)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(Date.from(Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY)))
                .signWith(SignatureAlgorithm.HS256, signingKey)
                .compact();
    }

    public Long validateAndExtractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (ExpiredJwtException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
            // 모든 토큰 에러를 하나로 처리
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}

