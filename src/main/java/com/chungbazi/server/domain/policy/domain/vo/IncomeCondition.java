package com.chungbazi.server.domain.policy.domain.vo;

import com.chungbazi.server.domain.policy.domain.vo.IncomeConditionType;

public record IncomeCondition(
        IncomeConditionType type,
        Integer minIncome,
        Integer maxIncome,
        String description
) {
}
