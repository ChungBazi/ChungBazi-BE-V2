package com.chungbazi.server.global.security.jwt;

import com.chungbazi.server.domain.auth.exception.AuthException;
import com.chungbazi.server.domain.auth.exception.code.AuthErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    public String createAccessToken(Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.accessExp());

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuer(jwtProperties.issuer())
                .setAudience(jwtProperties.audience())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.refreshExp());

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuer(jwtProperties.issuer())
                .setAudience(jwtProperties.audience())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims getClaims(String token) {
        try {
            return createParser()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public boolean validateToken(String token) {
        try {
            createParser().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new AuthException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (Exception e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    public Duration getRemainingExpiration(String token) {
        Date expiration = getClaims(token).getExpiration();
        long remainingMillis = expiration.getTime() - System.currentTimeMillis();

        return Duration.ofMillis(Math.max(remainingMillis, 0));
    }

    private JwtParser createParser() {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)))
                .requireIssuer(jwtProperties.issuer())
                .requireAudience(jwtProperties.audience())
                .build();
    }
}
