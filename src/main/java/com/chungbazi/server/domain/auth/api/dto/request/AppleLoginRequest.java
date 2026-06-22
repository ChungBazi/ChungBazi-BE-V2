package com.chungbazi.server.domain.auth.api.dto.request;

import lombok.Builder;

@Builder
public record AppleLoginRequest(
        String idToken,
        String email,
        String name,
        String fcmToken
) {
}
