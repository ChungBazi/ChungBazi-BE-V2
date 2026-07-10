package com.chungbazi.server.domain.policy.api.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record SearchSuggestionResponse(
        List<String> keywords
) {
    public static SearchSuggestionResponse of(List<String> keywords) {
        return SearchSuggestionResponse.builder()
                .keywords(keywords)
                .build();
    }
}
