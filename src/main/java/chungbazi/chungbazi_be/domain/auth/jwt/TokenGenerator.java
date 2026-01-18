package chungbazi.chungbazi_be.domain.auth.jwt;

import chungbazi.chungbazi_be.domain.auth.dto.TokenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class TokenGenerator {
    private static final String BEARER_TYPE = "Bearer";

    @Value("${jwt.access-exp}")
    private long accessTokenExp;

    @Value("${jwt.refresh-exp}")
    private long refreshTokenExp;

    private final JwtProvider jwtProvider;

    public TokenDTO generate(Long userId, String userName, Boolean isFirst) {
        long now = System.currentTimeMillis();

        Date accessTokenExpiredAt = new Date(now + accessTokenExp);
        Date refreshTokenExpiredAt = new Date(now + refreshTokenExp);

        String subject = userId.toString();
        String accessToken = jwtProvider.accessTokenGenerate(subject, accessTokenExpiredAt);
        String refreshToken = jwtProvider.refreshTokenGenerate(subject, refreshTokenExpiredAt);

        return TokenDTO.of(
                accessToken,
                refreshToken,
                BEARER_TYPE,
                accessTokenExp / 1000L,
                refreshTokenExp / 1000L,
                userId,
                userName,
                isFirst
        );
    }
}