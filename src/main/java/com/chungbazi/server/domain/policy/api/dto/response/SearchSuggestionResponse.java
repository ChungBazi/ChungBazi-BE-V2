package com.chungbazi.server.domain.policy.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "정책 검색어 자동완성 조회 API")
@Builder
public record SearchSuggestionResponse(
        @Schema(description = "정책 제목 기반 자동완성 검색어 목록", example = "[\"청년 일자리 도약 장려금\", \"청년 월세 지원\"]")
        List<String> keywords
) {
    public static SearchSuggestionResponse of(List<String> keywords) {
        return SearchSuggestionResponse.builder()
                .keywords(keywords)
                .build();
    }
}
