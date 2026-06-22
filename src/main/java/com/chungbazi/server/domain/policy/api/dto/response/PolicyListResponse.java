package com.chungbazi.server.domain.policy.api.dto.response;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
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

    public static PolicyListResponse of(
            long totalCount,
            List<Policy> policies,
            Set<Long> likedPolicyIds,
            String nextCursor,
            boolean hasNext
    ) {
        List<PolicySummary> summaries = policies.stream()
                .map(policy -> PolicySummary.from(policy, likedPolicyIds))
                .toList();

        return PolicyListResponse.builder()
                .totalCount(totalCount)
                .policies(summaries)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    @Builder
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

        private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

        public static PolicySummary from(Policy policy, Set<Long> likedPolicyIds) {
            return PolicySummary.builder()
                    .policyId(policy.getId())
                    .category(policy.getCategory())
                    .categoryName(policy.getCategory().getDescription())
                    .dDay(formatDDay(policy))
                    .title(policy.getTitle())
                    .viewCount(policy.getViewCount())
                    .liked(likedPolicyIds.contains(policy.getId()))
                    .build();
        }

        private static String formatDDay(Policy policy) {
            if (policy.getRecruitmentType() == RecruitmentType.ALWAYS) {
                return "상시";
            }
            if (policy.getApplyEndDate() == null) {
                return "미정";
            }

            long remainingDays = ChronoUnit.DAYS.between(
                    LocalDate.now(SERVICE_ZONE_ID),
                    policy.getApplyEndDate()
            );
            if (remainingDays < 0) {
                return "마감";
            }
            if (remainingDays == 0) {
                return "D-Day";
            }
            return "D-" + remainingDays;
        }
    }
}
