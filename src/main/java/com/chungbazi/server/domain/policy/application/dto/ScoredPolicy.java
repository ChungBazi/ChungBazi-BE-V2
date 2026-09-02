package com.chungbazi.server.domain.policy.application.dto;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import lombok.Builder;

@Builder
public record ScoredPolicy(
        Policy policy,
        int score
) {
    public static ScoredPolicy of(Policy policy, int score) {
        return ScoredPolicy.builder()
                .policy(policy)
                .score(score)
                .build();
    }
}
