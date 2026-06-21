package com.chungbazi.server.domain.policy.api.dto.response;

import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "분야별 정책 무한스크롤 응답")
public record PolicyListResponse(
        @Schema(description = "해당 분야의 전체 정책 수", example = "128")
        long totalCount,

        @Schema(description = "조회된 정책 목록")
        List<PolicySummary> policies,

        @Schema(description = "다음 페이지 조회 커서. 다음 페이지가 없으면 null", nullable = true)
        String nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {

    @Builder
    @Schema(description = "정책 목록 항목")
    public record PolicySummary(
            @Schema(description = "정책 ID", example = "42")
            Long policyId,

            @Schema(description = "정책 분야", example = "JOB_STARTUP")
            PolicyCategoryType category,

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
