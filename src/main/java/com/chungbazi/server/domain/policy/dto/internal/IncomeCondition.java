package com.chungbazi.server.domain.policy.dto.internal;

import com.chungbazi.server.domain.policy.enums.IncomeConditionType;

public record IncomeCondition(
        IncomeConditionType type,
        Integer minIncome,
        Integer maxIncome,
        String description
) {
}
