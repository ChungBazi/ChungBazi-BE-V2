package chungbazi.chungbazi_be.domain.auth.jwt;

import chungbazi.chungbazi_be.domain.auth.dto.TokenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class TokenGenerator {
    private static final String BEARER_TYPE = "Bearer";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 48; // 2일
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일

    private final JwtProvider jwtProvider;

    public TokenDTO generate(Long userId, String userName, Boolean isFirst) {

        long now = (new Date()).getTime();
        Date accessTokenExpiredAt = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        Date refreshTokenExpiredAt = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);

        String subject = userId.toString();
        String accessToken = jwtProvider.accessTokenGenerate(subject, accessTokenExpiredAt);
        String refreshToken = jwtProvider.refreshTokenGenerate(subject, refreshTokenExpiredAt);

        return TokenDTO.of(
                accessToken,
                refreshToken,
                BEARER_TYPE,
                ACCESS_TOKEN_EXPIRE_TIME / 1000L,
                REFRESH_TOKEN_EXPIRE_TIME / 1000L,
                userId,
                userName,
                isFirst);
    }
}