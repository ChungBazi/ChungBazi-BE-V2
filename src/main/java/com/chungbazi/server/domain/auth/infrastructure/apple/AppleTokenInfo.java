package com.chungbazi.server.domain.auth.infrastructure.apple;

import lombok.Builder;

@Builder
public record AppleTokenInfo(
        String providerId,
        String email
) {
    public static AppleTokenInfo of(String providerId, String email) {
        return AppleTokenInfo.builder()
                .providerId(providerId)
                .email(email)
                .build();
    }
}
