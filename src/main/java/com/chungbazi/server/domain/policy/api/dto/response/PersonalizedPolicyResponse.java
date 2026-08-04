package com.chungbazi.server.domain.policy.api.dto.response;

import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse.PolicySummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "카테고리별 맞춤 정책 응답")
@Builder
public record PersonalizedPolicyResponse(
        @Schema(description = "맞춤 정책 목록 (최대 5개)")
        List<PolicySummary> policies
) {
        public static PersonalizedPolicyResponse of(List<PolicySummary> policies) {
                return PersonalizedPolicyResponse.builder()
                        .policies(policies)
                        .build();
        }
}
