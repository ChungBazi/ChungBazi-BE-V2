package com.chungbazi.server.domain.auth.application;

import com.chungbazi.server.domain.auth.api.dto.request.AppleLoginRequest;
import com.chungbazi.server.domain.auth.api.dto.request.KakaoLoginRequest;
import com.chungbazi.server.domain.auth.api.dto.response.AuthTokenResponse;
import com.chungbazi.server.domain.auth.exception.AuthException;
import com.chungbazi.server.domain.auth.exception.code.AuthErrorCode;
import com.chungbazi.server.domain.auth.infrastructure.apple.AppleTokenInfo;
import com.chungbazi.server.domain.auth.infrastructure.apple.AppleTokenVerifier;
import com.chungbazi.server.domain.auth.infrastructure.kakao.KakaoClient;
import com.chungbazi.server.domain.auth.infrastructure.kakao.KakaoUserInfo;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.type.SocialType;
import com.chungbazi.server.domain.user.infrastructure.UserRepository;
import com.chungbazi.server.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

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
        throw new AuthException(AuthErrorCode.INVALID_TOKEN);
    }

    private String resolveAppleName(String requestName) {
        if (requestName != null && !requestName.isBlank()) {
            return requestName;
        }
        return null;
    }
}
