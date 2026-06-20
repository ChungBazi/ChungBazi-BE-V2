package com.chungbazi.server.domain.auth.application;

import com.chungbazi.server.domain.auth.api.dto.request.KakaoLoginRequest;
import com.chungbazi.server.domain.auth.api.dto.response.AuthTokenResponse;
import com.chungbazi.server.domain.auth.infrastructure.KakaoClient;
import com.chungbazi.server.domain.auth.infrastructure.KakaoUserInfo;
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

    @Transactional
    public AuthTokenResponse loginWithKakao(KakaoLoginRequest request) {
        KakaoUserInfo userInfo = kakaoClient.getUserInfo(request.accessToken());

        return loginOrSignUp(
                userInfo.getProviderId(),
                SocialType.KAKAO,
                userInfo.getEmail(),
                userInfo.getName(),
                request.fcmToken()
        );
    }

    private AuthTokenResponse loginOrSignUp(
            String providerId,
            SocialType socialType,
            String email,
            String nickname,
            String fcmToken
    ) {
        User user = userRepository.findByProviderId(providerId)
                .orElseGet(() -> userRepository.save(
                        User.create(
                                providerId,
                                socialType,
                                email,
                                nickname,
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
}
