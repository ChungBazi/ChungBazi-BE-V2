package com.chungbazi.server.domain.auth.api.dto.request;

import lombok.Builder;

@Builder
public record KakaoLoginRequest(
        String accessToken,
        String fcmToken
) {
}
