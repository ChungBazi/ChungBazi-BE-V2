package com.chungbazi.server.domain.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "사용자 이름 수정 API")
public record UserNameRequest(
        @NotBlank
        @Schema(description = "사용자 이름", example = "바로")
        String name
) {
}
