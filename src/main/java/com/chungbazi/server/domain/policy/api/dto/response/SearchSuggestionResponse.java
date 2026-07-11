package com.chungbazi.server.domain.policy.api.dto.response;

import com.chungbazi.server.domain.policy.domain.type.SearchSuggestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
@Schema(description = "정책 검색어 자동완성 조회 API")
public record SearchSuggestionResponse(
        @Schema(description = "자동완성 목록")
        List<Suggestion> suggestions
) {
    public static SearchSuggestionResponse of(
            List<String> recentKeywords,
            List<String> policyKeywords
    ) {
        List<Suggestion> suggestions = new ArrayList<>();

        recentKeywords.stream()
                .map(keyword -> Suggestion.of(SearchSuggestionType.RECENT_KEYWORD, keyword))
                .forEach(suggestions::add);

        policyKeywords.stream()
                .map(keyword -> Suggestion.of(SearchSuggestionType.POLICY_KEYWORD, keyword))
                .forEach(suggestions::add);

        return SearchSuggestionResponse.builder()
                .suggestions(suggestions)
                .build();
    }

    @Builder
    @Schema(description = "자동완성 항목")
    public record Suggestion(
            @Schema(description = "자동완성 타입", example = "RECENT_KEYWORD")
            SearchSuggestionType type,

            @Schema(description = "자동완성 키워드", example = "청년 일자리 도약 장려금")
            String keyword
    ) {
        public static Suggestion of(SearchSuggestionType type, String keyword) {
            return Suggestion.builder()
                    .type(type)
                    .keyword(keyword)
                    .build();
        }
    }
}
