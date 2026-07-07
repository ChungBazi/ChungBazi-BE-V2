package com.chungbazi.server.domain.policy.api.dto.response;

import com.chungbazi.server.domain.policy.domain.entity.RecentSearchPolicy;
import lombok.Builder;

import java.util.List;

@Builder
public record RecentSearchPolicyListResponse(
        List<RecentSearchPolicyResponse> keywords
) {
    public static RecentSearchPolicyListResponse from(List<RecentSearchPolicy> policies) {
        return RecentSearchPolicyListResponse.builder()
                .keywords(
                        policies.stream()
                                .map(RecentSearchPolicyResponse::from)
                                .toList()
                )
                .build();
    }

    @Builder
    public record RecentSearchPolicyResponse(
            Long keywordId,
            String keyword
    ) {
        public static RecentSearchPolicyResponse from(RecentSearchPolicy recentSearchPolicy) {
            return RecentSearchPolicyResponse.builder()
                    .keywordId(recentSearchPolicy.getId())
                    .keyword(recentSearchPolicy.getKeyword())
                    .build();
        }
    }
}
