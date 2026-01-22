package chungbazi.chungbazi_be.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenCacheService {

    private static final String FCM_TOKEN_PREFIX = "fcm:token:";
    private static final Duration TOKEN_TTL = Duration.ofDays(30);

    private final RedisTemplate<String, String> redisTemplate;

    public void cacheToken(Long userId, String token) {
        try {
            String key = FCM_TOKEN_PREFIX + userId;
            redisTemplate.opsForValue().set(key, token, TOKEN_TTL);

            log.debug("FCM 토큰 캐시 저장 완료: userId={}, token={}", userId, maskToken(token));
        } catch (Exception e) {
            log.error("FCM 토큰 캐시 저장 실패: userId={}", userId, e);
        }
    }

    public void refreshTokenTtl(Long userId) {
        try {
            String key = FCM_TOKEN_PREFIX + userId;
            redisTemplate.expire(key, TOKEN_TTL);

            log.debug("FCM 토큰 TTL 갱신 완료");
        } catch (Exception e) {
            log.error("FCM 토큰 TTL 갱신 실패", e);
        }
    }

    public String getTokenByUserId(Long userId) {
        try {
            String key = FCM_TOKEN_PREFIX + userId;
            String token = redisTemplate.opsForValue().get(key);
            return token;
        } catch (Exception e) {
            log.error("FCM 토큰 캐시 조회 실패", e);
            return null;
        }
    }

    public void deleteToken(Long userId) {
        try {
            String key = FCM_TOKEN_PREFIX + userId;

            redisTemplate.delete(key);

            log.debug("FCM 토큰 캐시 삭제 완료");
        } catch (Exception e) {
            log.error("FCM 토큰 캐시 삭제 실패", e);
        }
    }

    public boolean existsToken(Long userId) {
        try {
            String key = FCM_TOKEN_PREFIX + userId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("FCM 토큰 존재 여부 확인 실패", e);
            return false;
        }
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 10) + "...";
    }
}
