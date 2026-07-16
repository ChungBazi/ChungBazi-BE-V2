package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import org.springframework.stereotype.Component;

@Component
public class PolicyPopularityCalculator {

    private static final long SAVE_COUNT_WEIGHT = 5L;

    public static long calculate(Policy policy) {
        return policy.getViewCount() + policy.getSaveCount() * SAVE_COUNT_WEIGHT;
    }

    public static NumberExpression<Long> expression(
            NumberPath<Integer> viewCount,
            NumberPath<Integer> saveCount
    ) {
        return viewCount.longValue()
                .add(saveCount.longValue().multiply(SAVE_COUNT_WEIGHT));
    }
}
