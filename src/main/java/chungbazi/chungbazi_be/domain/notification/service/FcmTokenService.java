package chungbazi.chungbazi_be.domain.notification.service;

import chungbazi.chungbazi_be.domain.notification.entity.FcmToken;
import chungbazi.chungbazi_be.domain.notification.repository.FcmTokenRepository;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;
    private final FcmTokenCacheService cacheService;

    @Transactional
    public void registerOrUpdateToken(User user, String token) {
        FcmToken fcmToken = getByUserId(user.getId())
                .orElse(null);

        if (fcmToken != null) {
            updateTokenUsage(fcmToken);
        } else {
            fcmToken = FcmToken.builder()
                    .userId(user.getId())
                    .token(token)
                    .lastUsedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                    .build();
        }

        fcmTokenRepository.save(fcmToken);
        cacheService.cacheToken(user.getId(), token);

    }

    @Transactional(readOnly = true)
    public Optional<FcmToken> getByUserId(Long userId) {
        return fcmTokenRepository.findByUserId(userId);
    }

    //토큰 갱신
    @Transactional
    public void updateTokenUsage(FcmToken fcmToken) {

        fcmToken.updateLastUsedAt();
        fcmTokenRepository.save(fcmToken);

        // Redis TTL 갱신
        cacheService.refreshTokenTtl(fcmToken.getUserId());

    }

    @Transactional
    public void deleteToken(Long userId) {

        FcmToken fcmToken = fcmTokenRepository.findByUserId(userId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_FCM_TOKEN));

        // DB에서 삭제
        fcmTokenRepository.delete(fcmToken);

        //redis 캐시에서 삭제
        cacheService.deleteToken(fcmToken.getUserId());

    }

    @Transactional
    public String getFcmToken(Long userId){

        //캐시에 fcm 토큰이 존재할 경우
        if (cacheService.existsToken(userId)) {
            return cacheService.getTokenByUserId(userId);
        } else {
            FcmToken fcmToken = getByUserId(userId)
                    .orElseThrow(()-> new GeneralException(ErrorStatus.NOT_FOUND_FCM_TOKEN));

            cacheService.cacheToken(userId, fcmToken.getToken());
            return fcmToken.getToken();
        }

    }

}
