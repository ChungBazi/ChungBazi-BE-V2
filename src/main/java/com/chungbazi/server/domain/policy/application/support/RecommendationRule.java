package com.chungbazi.server.domain.policy.application.support;

import java.util.function.Predicate;

public record RecommendationRule(
        String name,
        int score,
        Predicate<RecommendationInput> condition
) {
    public int evaluate(RecommendationInput input) {
        return condition.test(input) ? score : 0;
    }
}
