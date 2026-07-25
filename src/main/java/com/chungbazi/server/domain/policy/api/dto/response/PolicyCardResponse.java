package com.chungbazi.server.domain.policy.api.dto.response;

import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import io.swagger.v3.oas.annotations.media.Schema;

public record PolicyCardResponse(
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

        @Schema(description = "신청 기간", example = "2025.05.03 - 2025.06.30")
        String applyPeriod,

        @Schema(description = "정책 한 줄 소개", example = "소속 근로자가 일·생활 균형을 위해 유연근무제를 활용하게 하는 중소, 중견기업에게 장려금을 지원")
        String summary,

        @Schema(description = "정책 지원 내용", example = "서울 청년취업사관학교는 청년들의 실무 역량을 키우고 취업까지 이어질 수 있도록 돕는 교육 프로그램이에요. 디지털·IT 분야를 중심으로 ~~")
        String supportContent,

        @Schema(description = "신청 URL", example = "https://www.youthcenter.go.kr")
        String applyUrl,

        @Schema(description = "현재 사용자의 찜 여부", example = "true")
        boolean liked
) {
}
