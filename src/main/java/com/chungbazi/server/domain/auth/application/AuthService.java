package com.chungbazi.server.domain.auth.application;

import com.chungbazi.server.domain.auth.api.dto.request.AppleLoginRequest;
import com.chungbazi.server.domain.auth.api.dto.request.AuthReissueRequest;
import com.chungbazi.server.domain.auth.api.dto.request.KakaoLoginRequest;
import com.chungbazi.server.domain.auth.api.dto.response.AuthReissueResponse;
import com.chungbazi.server.domain.auth.api.dto.response.AuthTokenResponse;
import com.chungbazi.server.domain.auth.domain.RefreshToken;
import com.chungbazi.server.domain.auth.exception.AuthException;
import com.chungbazi.server.domain.auth.exception.code.AuthErrorCode;
import com.chungbazi.server.domain.auth.infrastructure.redis.RefreshTokenRepository;
import com.chungbazi.server.domain.auth.infrastructure.apple.AppleTokenInfo;
import com.chungbazi.server.domain.auth.infrastructure.apple.AppleTokenVerifier;
import com.chungbazi.server.domain.auth.infrastructure.kakao.KakaoClient;
import com.chungbazi.server.domain.auth.infrastructure.kakao.KakaoUserInfo;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.type.SocialType;
import com.chungbazi.server.domain.user.exception.UserException;
import com.chungbazi.server.domain.user.exception.code.UserErrorCode;
import com.chungbazi.server.domain.user.infrastructure.UserRepository;
import com.chungbazi.server.global.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final KakaoClient kakaoClient;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final AppleTokenVerifier appleTokenVerifier;
    private final TokenBlacklist tokenBlacklist;

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

    @Transactional
    public AuthReissueResponse reissueToken(AuthReissueRequest request) {
        jwtProvider.validateToken(request.refreshToken());

        User user = getUserFromToken(request.refreshToken());

        RefreshToken savedToken = validateRefreshToken(user.getId(), request.refreshToken());

        String newAccessToken = jwtProvider.createAccessToken(user.getId());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());

        savedToken.updateRefreshToken(newRefreshToken);
        refreshTokenRepository.save(savedToken);

        return AuthReissueResponse.of(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId, String accessToken) {
        Duration remainingExpiration = jwtProvider.getRemainingExpiration(accessToken);

        if (!remainingExpiration.isZero()) {
            tokenBlacklist.add(accessToken, remainingExpiration);
        }
        refreshTokenRepository.deleteById(userId);
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

        refreshTokenRepository.save(RefreshToken.create(user.getId(), refreshToken));

        return AuthTokenResponse.of(
                accessToken,
                refreshToken,
                user.getEmail(),
                user.getSocialType(),
                user.isOnboardingCompleted()
        );
    }

    private RefreshToken validateRefreshToken(Long userId, String token) {
        RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!savedToken.getRefreshToken().equals(token)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
        return savedToken;
    }

    private User getUserFromToken(String token) {
        Claims claims = jwtProvider.getClaims(token);
        String userId = claims.getSubject();

        return userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
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
