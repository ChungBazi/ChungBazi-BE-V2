package com.chungbazi.server.domain.auth.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record AuthReissueResponse(
        @Schema(
                description = "새로 발급된 accessToken",
                example = "eyJh.eqi57hK"
        )
        String accessToken,

        @Schema(
                description = "새로 발급된 refreshToken",
                example = "eyJh.eqi57hK"
        )
        String refreshToken
) {
    public static AuthReissueResponse of(String accessToken, String refreshToken) {
        return AuthReissueResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
