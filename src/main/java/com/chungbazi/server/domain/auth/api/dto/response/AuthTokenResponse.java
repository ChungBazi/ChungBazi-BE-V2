package com.chungbazi.server.domain.auth.api.dto.response;

import com.chungbazi.server.domain.user.domain.type.SocialType;
import lombok.Builder;

@Builder
public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        String email,
        SocialType socialType,
        boolean onboardingCompleted
) {
    public static AuthTokenResponse of(
            String accessToken,
            String refreshToken,
            String email,
            SocialType socialType,
            boolean onboardingCompleted
    ) {
        return AuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(email)
                .socialType(socialType)
                .onboardingCompleted(onboardingCompleted)
                .build();
    }
}
