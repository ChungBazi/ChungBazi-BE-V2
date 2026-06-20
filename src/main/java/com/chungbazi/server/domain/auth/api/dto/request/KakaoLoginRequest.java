package com.chungbazi.server.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record KakaoLoginRequest(
        @Schema(description = "iOS Kakao SDK에서 발급받은 카카오 accessToken")
        String accessToken,

        @Schema(description = "현재 사용자 기기의 fcmToken")
        String fcmToken
) {
}
