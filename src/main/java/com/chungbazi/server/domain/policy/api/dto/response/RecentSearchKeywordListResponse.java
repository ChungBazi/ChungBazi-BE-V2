package com.chungbazi.server.domain.policy.api.dto.response;

import com.chungbazi.server.domain.policy.domain.entity.RecentSearchKeyword;
import com.chungbazi.server.domain.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "최근 검색어 목록 조회 API")
@Builder
public record RecentSearchKeywordListResponse(
        @Schema(description = "최근 검색어 자동 저장 여부", example = "true")
        boolean autoSaveEnabled,

        @Schema(description = "최근 검색어 목록")
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
    @Schema(description = "최근 검색어 항목")
    public record RecentSearchKeywordResponse(
            @Schema(description = "최근 검색어 ID", example = "1")
            Long keywordId,

            @Schema(description = "검색어", example = "청년 월세")
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
