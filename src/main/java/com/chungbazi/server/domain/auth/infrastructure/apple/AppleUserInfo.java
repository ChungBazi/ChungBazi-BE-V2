package com.chungbazi.server.domain.auth.infrastructure.apple;

import com.chungbazi.server.domain.auth.domain.OAuth2UserInfo;

public record AppleUserInfo(
        String providerId,
        String email,
        String name
) implements OAuth2UserInfo {
    @Override
    public String getProviderId() {
        return providerId;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getName() {
        return name;
    }
}
