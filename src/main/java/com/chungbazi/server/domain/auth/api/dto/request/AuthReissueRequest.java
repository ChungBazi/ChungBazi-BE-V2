package com.chungbazi.server.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record AuthReissueRequest(
        @Schema(
                description = "유효한 refreshToken",
                example = "eyJh.eqi57hK"
        )
        String refreshToken
) {
}
