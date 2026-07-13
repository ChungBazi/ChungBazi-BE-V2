package com.chungbazi.server.domain.policy.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "최근 검색어 자동 저장 설정 변경 API")
@Builder
public record SearchKeywordAutoSaveRequest(
        @Schema(description = "최근 검색어 자동 저장 여부", example = "true")
        boolean enabled
) {
}
