package com.chungbazi.server.domain.auth.api.dto.response;

import com.chungbazi.server.domain.user.domain.type.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record AuthTokenResponse(
        @Schema(
                description = "청바지 accessToken",
                example = "eyJhbGciOiJIUzI1NiJ9.ey..."
        )
        String accessToken,

        @Schema(
                description = "청바지 refreshToken",
                example = "eyJhbGciOiJIUzI1NiJ9.ey..."
        )
        String refreshToken,

        @Schema(
                description = "사용자 이메일",
                example = "user@example.com"
        )
        String email,

        @Schema(
                description = "소셜 로그인 타입",
                example = "KAKAO"
        )
        SocialType socialType,

        @Schema(
                description = "온보딩 완료 여부",
                example = "false"
        )
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
