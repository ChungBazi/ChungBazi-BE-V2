package com.chungbazi.server.domain.auth.infrastructure.redis;

import com.chungbazi.server.domain.auth.application.TokenBlacklist;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisTokenBlacklist implements TokenBlacklist {

    private static final String PREFIX = "accessTokenBlacklist:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void add(String accessToken, Duration expiration) {
        redisTemplate.opsForValue().set(
                PREFIX + accessToken,
                "logout",
                expiration
        );
    }

    @Override
    public boolean contains(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + accessToken));
    }
}
