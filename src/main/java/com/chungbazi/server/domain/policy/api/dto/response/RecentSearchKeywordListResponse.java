package com.chungbazi.server.domain.policy.api.dto.response;

import com.chungbazi.server.domain.policy.application.cursor.RecentSearchKeywordCursorParser;
import com.chungbazi.server.domain.policy.domain.entity.RecentSearchKeyword;
import com.chungbazi.server.domain.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
@Schema(description = "최근 검색어 목록 조회 API")
public record RecentSearchKeywordListResponse(
        @Schema(description = "최근 검색어 자동 저장 여부", example = "true")
        boolean autoSaveEnabled,

        @Schema(description = "최근 검색어 목록")
        List<RecentSearchKeywordResponse> keywords,

        @Schema(description = "다음 페이지 조회 커서. 다음 페이지가 없으면 null", nullable = true)
        String nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
    public static RecentSearchKeywordListResponse of(
            User user,
            List<RecentSearchKeyword> fetchedKeywords,
            int size
    ) {
        boolean hasNext = fetchedKeywords.size() > size;

        List<RecentSearchKeyword> keywords = hasNext
                ? new ArrayList<>(fetchedKeywords.subList(0, size))
                : fetchedKeywords;

        String nextCursor = hasNext
                ? RecentSearchKeywordCursorParser.encode(keywords.getLast())
                : null;

        return RecentSearchKeywordListResponse.builder()
                .autoSaveEnabled(user.isSearchKeywordAutoSaveEnabled())
                .keywords(
                        keywords.stream()
                                .map(RecentSearchKeywordResponse::from)
                                .toList()
                )
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
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
