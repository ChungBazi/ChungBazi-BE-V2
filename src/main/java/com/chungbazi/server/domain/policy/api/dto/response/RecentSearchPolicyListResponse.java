package com.chungbazi.server.domain.policy.api.dto.response;

import com.chungbazi.server.domain.policy.domain.entity.RecentSearchPolicy;
import com.chungbazi.server.domain.user.domain.User;
import lombok.Builder;

import java.util.List;

@Builder
public record RecentSearchPolicyListResponse(
        boolean autoSaveEnabled,
        List<RecentSearchPolicyResponse> keywords
) {
    public static RecentSearchPolicyListResponse of(
            User user,
            List<RecentSearchPolicy> policies
    ) {
        return RecentSearchPolicyListResponse.builder()
                .autoSaveEnabled(user.isSearchPolicyAutoSaveEnabled())
                .keywords(
                        policies.stream()
                                .map(RecentSearchPolicyResponse::from)
                                .toList()
                ).build();
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
