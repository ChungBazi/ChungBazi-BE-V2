package com.chungbazi.server.domain.policy.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "정책 카드 목록 응답")
public record PolicyCardListResponse(
        @Schema(description = "정책 카드 목록 (최대 20개)")
        List<PolicyCardResponse> policies
) {

    public static PolicyCardListResponse of(List<PolicyCardResponse> policies) {
        return PolicyCardListResponse.builder()
                .policies(policies)
                .build();
    }
}