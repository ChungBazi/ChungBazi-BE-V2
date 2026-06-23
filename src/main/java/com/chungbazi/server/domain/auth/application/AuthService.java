package com.chungbazi.server.domain.auth.application;

import com.chungbazi.server.domain.auth.api.dto.request.AppleLoginRequest;
import com.chungbazi.server.domain.auth.api.dto.request.KakaoLoginRequest;
import com.chungbazi.server.domain.auth.api.dto.response.AuthTokenResponse;
import com.chungbazi.server.domain.auth.infrastructure.apple.AppleTokenInfo;
import com.chungbazi.server.domain.auth.infrastructure.apple.AppleTokenVerifier;
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
        User user = loginOrSignUp(
                SocialType.KAKAO,
                userInfo.getProviderId(),
                userInfo.getEmail(),
                userInfo.getName(),
                request.fcmToken()
        );
        return issueTokenResponse(user);
    }

    @Transactional
    public AuthTokenResponse loginWithApple(AppleLoginRequest request) {
        AppleTokenInfo tokenInfo = appleTokenVerifier.verify(request.idToken());
        User user = loginOrSignUpWithApple(tokenInfo, request.name(), request.fcmToken());
        return issueTokenResponse(user);
    }

    private User loginOrSignUp(
            SocialType socialType,
            String providerId,
            String email,
            String name,
            String fcmToken
    ) {
        User user = userRepository.findBySocialTypeAndProviderId(socialType, providerId)
                .orElseGet(() -> userRepository.save(
                        User.create(
                                providerId,
                                socialType,
                                email,
                                name,
                                fcmToken
                        )
                ));
        user.updateFcmToken(fcmToken);

        return user;
    }

    private User loginOrSignUpWithApple(AppleTokenInfo tokenInfo, String name, String fcmToken) {
        User user = userRepository.findBySocialTypeAndProviderId(SocialType.APPLE, tokenInfo.providerId())
                .orElseGet(() -> userRepository.save(
                        User.create(
                                tokenInfo.providerId(),
                                SocialType.APPLE,
                                resolveAppleEmail(tokenInfo.email()),
                                resolveAppleName(name),
                                fcmToken
                        )
                ));
        user.updateFcmToken(fcmToken);

        return user;
    }

    private AuthTokenResponse issueTokenResponse(User user) {
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

    private String resolveAppleEmail(String tokenEmail) {
        if (tokenEmail != null && !tokenEmail.isBlank()) {
            return tokenEmail;
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
