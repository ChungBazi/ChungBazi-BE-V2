package com.chungbazi.server.domain.user.api.dto.response;

import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.type.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "사용자 정보 조회 API")
public record UserInfoResponse(
        @Schema(description = "사용자 이름", example = "주정빈")
        String name,

        @Schema(description = "사용자 이메일", example = "user@example.com")
        String email,

        @Schema(description = "소셜 로그인 타입", example = "KAKAO")
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
