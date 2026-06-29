package com.chungbazi.server.domain.user.api.dto.response;

import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.type.SocialType;
import lombok.Builder;

@Builder
public record UserInfoResponse(
        String name,
        String email,
        SocialType socialType
) {
    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .socialType(user.getSocialType())
                .build();
    }
}
