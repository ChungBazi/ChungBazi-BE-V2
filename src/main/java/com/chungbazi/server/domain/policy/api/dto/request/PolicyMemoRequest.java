package com.chungbazi.server.domain.policy.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "정책 메모 작성 및 수정 요청")
public record PolicyMemoRequest(
        @Schema(description = "메모 내용", example = "신청 전에 주민등록등본 준비하기")
        @NotNull
        String memo
) {
}
