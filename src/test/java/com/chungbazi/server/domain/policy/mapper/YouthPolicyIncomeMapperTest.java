package com.chungbazi.server.domain.policy.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.chungbazi.server.domain.policy.dto.internal.IncomeCondition;
import org.junit.jupiter.api.Test;

class YouthPolicyIncomeMapperTest {

    private final YouthPolicyIncomeMapper mapper = new YouthPolicyIncomeMapper();

    @Test
    void mapsNoLimitConditionToNullValues() {
        IncomeCondition result = mapper.toIncomeCondition("0043001", "0", "0", null);

        assertThat(result.minIncome()).isNull();
        assertThat(result.maxIncome()).isNull();
        assertThat(result.description()).isNull();
    }

    @Test
    void mapsAmountBasedConditionAndNormalizesZero() {
        IncomeCondition result = mapper.toIncomeCondition("0043002", "0", "6000", null);

        assertThat(result.minIncome()).isNull();
        assertThat(result.maxIncome()).isEqualTo(6000);
    }

    @Test
    void keepsTextForEtcConditionWithoutNumericLimits() {
        IncomeCondition result = mapper.toIncomeCondition(
                "0043003",
                "0",
                "0",
                "기준 중위소득 150% 이하"
        );

        assertThat(result.minIncome()).isNull();
        assertThat(result.maxIncome()).isNull();
        assertThat(result.description()).isEqualTo("기준 중위소득 150% 이하");
    }
}
