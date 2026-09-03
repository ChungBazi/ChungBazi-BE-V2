package com.chungbazi.server.domain.policy.infrastructure.search;

public record PolicySearchResult(
        Long policyId,
        float score
) {
}
