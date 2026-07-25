package com.chungbazi.server.domain.policy.api.dto.response;

import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PolicyDetailResponse(
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

        @Schema(description = "조회수", example = "153")
        int viewCount,

        @Schema(description = "현재 사용자의 찜 여부", example = "true")
        boolean liked,

        //TODO: 프론트한테 연락오고나서 본문 부분 처리

        @Schema(description = "맞춤 추천 정책 목록")
        List<PolicySummary> policies,

        @Schema(description = "같은 분야의 인기있는 정책 목록")
        List<PolicySummary> popularPolicies
) {

    @Schema(description = "정책 목록 항목")
    public record PolicySummary(
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

            @Schema(description = "조회수", example = "153")
            int viewCount,

            @Schema(description = "현재 사용자의 찜 여부", example = "true")
            boolean liked
    ) {
    }

}
