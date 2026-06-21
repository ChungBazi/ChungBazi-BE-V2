package com.chungbazi.server.domain.policy.domain.type.internal;

import com.chungbazi.server.domain.policy.domain.type.IncomeConditionType;

public record IncomeCondition(
        IncomeConditionType type,
        Integer minIncome,
        Integer maxIncome,
        String description
) {
}
