package com.chungbazi.server.domain.policy.api.dto.response;

import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse.PolicySummary;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "홈 화면 정책 섹션 응답")
public record HomePolicyResponse(
        @Schema(description = "최근 본 정책 목록")
        List<PolicySummary> recentViewedPolicies,

        @Schema(description = "인기 정책 목록")
        List<PolicySummary> popularPolicies,

        @Schema(description = "마감 임박 정책 목록")
        List<PolicySummary> upcomingDeadlinePolicies,

        @Schema(description = "최신 정책 목록")
        List<PolicySummary> latestPolicies
) {
}
