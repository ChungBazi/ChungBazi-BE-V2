package com.chungbazi.server.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "애플 로그인")
public record AppleLoginRequest(
        @Schema(
                description = "iOS Apple 로그인 성공 후 발급받은 identityToken",
                example = "YYrt9DQdv2djk6Q..."
        )
        @NotBlank
        String idToken,

        @Schema(
                description = "Apple에서 전달받은 이름, 최초 로그인 이후에는 null일 수 있음",
                example = "바로",
                nullable = true
        )
        String name,

        @Schema(
                description = "현재 사용자 기기의 fcmToken",
                example = "YYrt9DQdv2djk6Q..."
        )
        String fcmToken
) {
}
