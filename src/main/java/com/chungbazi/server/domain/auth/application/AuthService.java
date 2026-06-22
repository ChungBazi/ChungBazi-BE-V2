package com.chungbazi.server.domain.auth.application;

import com.chungbazi.server.domain.auth.api.dto.request.AppleLoginRequest;
import com.chungbazi.server.domain.auth.api.dto.request.KakaoLoginRequest;
import com.chungbazi.server.domain.auth.api.dto.response.AuthTokenResponse;
import com.chungbazi.server.domain.auth.domain.OAuth2UserInfo;
import com.chungbazi.server.domain.auth.infrastructure.apple.AppleTokenInfo;
import com.chungbazi.server.domain.auth.infrastructure.apple.AppleTokenVerifier;
import com.chungbazi.server.domain.auth.infrastructure.apple.AppleUserInfo;
import com.chungbazi.server.domain.auth.infrastructure.kakao.KakaoClient;
import com.chungbazi.server.domain.auth.infrastructure.kakao.KakaoUserInfo;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.type.SocialType;
import com.chungbazi.server.domain.user.infrastructure.UserRepository;
import com.chungbazi.server.global.common.code.exception.GeneralException;
import com.chungbazi.server.global.common.code.status.ErrorStatus;
import com.chungbazi.server.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String DEFAULT_APPLE_USER_NAME = "바로";
    private static final String[] DEFAULT_APPLE_USER_NAME_PREFIXES = {
            "든든한", "야무진", "똑똑한", "부지런한", "귀여운",
            "성실한", "희망찬", "알찬", "힘찬", "빛나는"
    };

    private final KakaoClient kakaoClient;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final AppleTokenVerifier appleTokenVerifier;

    @Transactional
    public AuthTokenResponse loginWithKakao(KakaoLoginRequest request) {
        KakaoUserInfo userInfo = kakaoClient.getUserInfo(request.accessToken());
        return loginOrSignUp(userInfo, SocialType.KAKAO, request.fcmToken());
    }

    @Transactional
    public AuthTokenResponse loginWithApple(AppleLoginRequest request) {
        AppleTokenInfo tokenInfo = appleTokenVerifier.verify(request.idToken());

        AppleUserInfo userInfo = AppleUserInfo.of(
                tokenInfo.providerId(),
                resolveAppleEmail(tokenInfo.email(), request.email()),
                resolveAppleName(request.name())
        );
        return loginOrSignUp(userInfo, SocialType.APPLE, request.fcmToken());
    }

    private AuthTokenResponse loginOrSignUp(
            OAuth2UserInfo userInfo,
            SocialType socialType,
            String fcmToken
    ) {
        User user = userRepository.findBySocialTypeAndProviderId(socialType, userInfo.getProviderId())
                .orElseGet(() -> userRepository.save(
                        User.create(
                                userInfo.getProviderId(),
                                socialType,
                                userInfo.getEmail(),
                                userInfo.getName(),
                                fcmToken
                        )
                ));
        user.updateFcmToken(fcmToken);

        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        return AuthTokenResponse.of(
                accessToken,
                refreshToken,
                user.getEmail(),
                user.getSocialType(),
                user.isOnboardingCompleted()
        );
    }

    private String resolveAppleEmail(String tokenEmail, String requestEmail) {
        if (tokenEmail != null && !tokenEmail.isBlank()) {
            return tokenEmail;
        }

        if (requestEmail != null && !requestEmail.isBlank()) {
            return requestEmail;
        }

        throw new GeneralException(ErrorStatus._INVALID_TOKEN);
    }

    private String resolveAppleName(String requestName) {
        if (requestName != null && !requestName.isBlank()) {
            return requestName;
        }

        return generateDefaultAppleName();
    }

    private String generateDefaultAppleName() {
        String prefix = DEFAULT_APPLE_USER_NAME_PREFIXES[
                ThreadLocalRandom.current().nextInt(DEFAULT_APPLE_USER_NAME_PREFIXES.length)
                ];
        return prefix + " " + DEFAULT_APPLE_USER_NAME;
    }
}
