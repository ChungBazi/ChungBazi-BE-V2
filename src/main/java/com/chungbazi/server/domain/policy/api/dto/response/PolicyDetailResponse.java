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

        @Schema(description = "신청 자격 설명", example = "연령: 20세 이상 40세 미만\n소득: 9분위")
        String eligibilityDescription,

        @Schema(description = "신청 기간", example = "2024년 12월 9일 ~ 2025년 1월 31일")
        String applyPeriod,

        @Schema(description = "지원 내용", example = "월세근무제 활용 장려금을 지원합니다.")
        String supportContent,

        @Schema(description = "신청 방법", example = "신청인 본인 주민등록지 동 주민센터 방문접수")
        String applicationMethod,

        @Schema(description = "제출 서류", example = "없음")
        String submittedDocument,

        @Schema(description = "심사 및 결과 안내", example = "선발 및 연수기관 매칭결과 개별 안내")
        String screeningMethod,

        @Schema(description = "참고 URL 목록")
        List<String> referenceUrls,

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
