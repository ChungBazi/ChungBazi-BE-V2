package com.chungbazi.server.domain.policy.infrastructure.search;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import lombok.Builder;

@Builder
public record PolicySearchDocument(
        Long policyId,
        String title,
        String summary,
        String supportContent
) {
    public static PolicySearchDocument from(Policy policy) {
        return PolicySearchDocument.builder()
                .policyId(policy.getId())
                .title(policy.getTitle())
                .summary(policy.getSummary())
                .supportContent(policy.getSupportContent())
                .build();
    }
}
