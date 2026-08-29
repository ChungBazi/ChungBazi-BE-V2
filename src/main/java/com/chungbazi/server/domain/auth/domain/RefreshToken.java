package com.chungbazi.server.domain.auth.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@NoArgsConstructor
@RedisHash(value = "refreshToken")
public class RefreshToken {

    private static final long EXPIRATION_SECONDS = 7 * 24 * 60 * 60;

    @Id
    private Long userId;

    private String refreshToken;

    @TimeToLive
    private Long expiration;

    public static RefreshToken create(Long userId, String refreshToken) {
        RefreshToken token = new RefreshToken();
        token.userId = userId;
        token.refreshToken = refreshToken;
        token.expiration = EXPIRATION_SECONDS;
        return token;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
