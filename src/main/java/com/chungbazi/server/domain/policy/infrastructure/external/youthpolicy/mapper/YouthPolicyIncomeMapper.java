package com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper;

import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.domain.type.internal.IncomeCondition;
import com.chungbazi.server.domain.policy.domain.type.IncomeConditionType;
import org.springframework.stereotype.Component;

@Component
public class YouthPolicyIncomeMapper {

    static final String NO_LIMIT_CODE = "0043001";
    static final String AMOUNT_BASED_CODE = "0043002";
    static final String ETC_CONDITION_CODE = "0043003";

    public IncomeCondition toIncomeCondition(YouthPolicyItem item) {
        return toIncomeCondition(
                item.earnCndSeCd(),
                item.earnMinAmt(),
                item.earnMaxAmt(),
                item.earnEtcCn()
        );
    }

    IncomeCondition toIncomeCondition(
            String conditionCode,
            String minIncome,
            String maxIncome,
            String description
    ) {
        String normalizedCode = YouthPolicyTextUtils.trimToNull(conditionCode);
        String normalizedDescription = YouthPolicyTextUtils.trimToNull(description);

        if (NO_LIMIT_CODE.equals(normalizedCode)) {
            return new IncomeCondition(IncomeConditionType.NO_LIMIT,null, null, null);
        }

        if (AMOUNT_BASED_CODE.equals(normalizedCode)) {
            return new IncomeCondition(
                    IncomeConditionType.AMOUNT_BASED,
                    parsePositiveInteger(minIncome),
                    parsePositiveInteger(maxIncome),
                    normalizedDescription
            );
        }
        if (ETC_CONDITION_CODE.equals(normalizedCode)) {
            return new IncomeCondition(IncomeConditionType.OTHER,null, null, normalizedDescription);
        }

        return new IncomeCondition(
                IncomeConditionType.OTHER,
                parsePositiveInteger(minIncome),
                parsePositiveInteger(maxIncome),
                normalizedDescription
        );
    }

    private Integer parsePositiveInteger(String value) {
        Integer parsedValue = YouthPolicyTextUtils.parseInteger(value);
        return parsedValue != null && parsedValue > 0 ? parsedValue : null;
    }
}
