package com.chungbazi.server.domain.user.api.dto.response;

import com.chungbazi.server.domain.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "온보딩 완료 응답")
public record UserOnboardingResponse(
        @Schema(description = "사용자 닉네임", example = "청바지")
        String nickname
) {
    public static UserOnboardingResponse from(User user) {
        return new UserOnboardingResponse(user.getName());
    }
}
