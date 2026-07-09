package com.chungbazi.server.domain.policy.api.dto.response;

import com.chungbazi.server.domain.policy.domain.entity.RecentSearchKeyword;
import com.chungbazi.server.domain.user.domain.User;
import lombok.Builder;

import java.util.List;

@Builder
public record RecentSearchKeywordListResponse(
        boolean autoSaveEnabled,
        List<RecentSearchKeywordResponse> keywords
) {
    public static RecentSearchKeywordListResponse of(
            User user,
            List<RecentSearchKeyword> keywords
    ) {
        return RecentSearchKeywordListResponse.builder()
                .autoSaveEnabled(user.isSearchKeywordAutoSaveEnabled())
                .keywords(
                        keywords.stream()
                                .map(RecentSearchKeywordResponse::from)
                                .toList()
                ).build();
    }

    @Builder
    public record RecentSearchKeywordResponse(
            Long keywordId,
            String keyword
    ) {
        public static RecentSearchKeywordResponse from(RecentSearchKeyword recentSearchKeyword) {
            return RecentSearchKeywordResponse.builder()
                    .keywordId(recentSearchKeyword.getId())
                    .keyword(recentSearchKeyword.getKeyword())
                    .build();
        }
    }
}
