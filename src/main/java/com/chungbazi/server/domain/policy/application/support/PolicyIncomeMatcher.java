package com.chungbazi.server.domain.policy.application.support;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.type.IncomeConditionType;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.type.IncomeLevel;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PolicyIncomeMatcher {

    private static final Pattern MAX_INCOME_LEVEL_PATTERN = Pattern.compile("(\\d{1,2})\\s*분위\\s*이하");

    public IncomeMatchResult match(User user, Policy policy) {
        IncomeLevel userIncomeLevel = user.getIncomeLevel();

        if (userIncomeLevel == null || userIncomeLevel == IncomeLevel.UNKNOWN) {
            return IncomeMatchResult.UNKNOWN;
        }

        // 연소득 금액과 소득 제한 없음은 소득분위 기반 개인화 점수로 비교 X
        if (policy.getIncomeConditionType() != IncomeConditionType.OTHER) {
            return IncomeMatchResult.UNKNOWN;
        }

        return matchDescription(userIncomeLevel, policy.getIncomeDescription());
    }

    private IncomeMatchResult matchDescription(IncomeLevel userIncomeLevel, String description) {
        if (description == null || description.isBlank()) {
            return IncomeMatchResult.UNKNOWN;
        }

        // description에 분위 관련 내용 나오면 파싱
        Matcher matcher = MAX_INCOME_LEVEL_PATTERN.matcher(description);

        if (!matcher.find()) {
            return IncomeMatchResult.UNKNOWN;
        }

        int policyMaxLevel = Integer.parseInt(matcher.group(1));

        // 유형별로 서로 다른 분위 조건이 있는 복합 문구는 자동 판정 X
        if (matcher.find() || policyMaxLevel < 1 || policyMaxLevel > 10) {
            return IncomeMatchResult.UNKNOWN;
        }

        return toNumber(userIncomeLevel) <= policyMaxLevel
                ? IncomeMatchResult.MATCH
                : IncomeMatchResult.MISMATCH;
    }

    private int toNumber(IncomeLevel incomeLevel) {
        return switch (incomeLevel) {
            case LEVEL_1 -> 1;
            case LEVEL_2 -> 2;
            case LEVEL_3 -> 3;
            case LEVEL_4 -> 4;
            case LEVEL_5 -> 5;
            case LEVEL_6 -> 6;
            case LEVEL_7 -> 7;
            case LEVEL_8 -> 8;
            case LEVEL_9 -> 9;
            case LEVEL_10 -> 10;
            case UNKNOWN -> throw new IllegalArgumentException("UNKNOWN 소득분위는 비교할 수 없습니다.");
        };
    }
}
