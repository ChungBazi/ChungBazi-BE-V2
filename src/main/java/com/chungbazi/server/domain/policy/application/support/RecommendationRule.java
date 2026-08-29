package com.chungbazi.server.domain.policy.application.support;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public record RecommendationRule(
        String name,
        ToIntFunction<RecommendationInput> scoreFunction
) {
    public int evaluate(RecommendationInput input) {
        return scoreFunction.applyAsInt(input);
    }

    public static RecommendationRule of(
            String name,
            ToIntFunction<RecommendationInput> scoreFunction
    ) {
        return new RecommendationRule(name, scoreFunction);
    }

    public static RecommendationRule fixed(
            String name,
            int score,
            Predicate<RecommendationInput> condition
    ) {
        return of(
                name,
                input -> condition.test(input) ? score : 0
        );
    }
}
