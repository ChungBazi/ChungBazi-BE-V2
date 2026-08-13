package com.chungbazi.server.domain.policy.api.dto.response;

import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "정책 메모 조회 응답")
public record PolicyMemoResponse(
        @Schema(description = "정책 ID", example = "42")
        Long policyId,

        @Schema(description = "정책 분야", example = "JOB_STARTUP")
        PolicyCategoryType category,

        @Schema(description = "정책 분야 표시명", example = "취업/창업")
        String categoryName,

        @Schema(description = "마감 표시", example = "D-7")
        String dDay,

        @Schema(description = "정책 제목", example = "청년 일자리 도약 장려금")
        String title,

        @Schema(description = "메모 내용", example = "신청 전에 주민등록등본 준비하기")
        String memo
) {
}
