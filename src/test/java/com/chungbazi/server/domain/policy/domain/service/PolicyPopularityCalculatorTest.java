package com.chungbazi.server.domain.policy.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.chungbazi.server.domain.policy.application.PolicyPopularityCalculator;
import com.chungbazi.server.domain.policy.application.cursor.PolicyCursor;
import com.chungbazi.server.domain.policy.application.cursor.PolicyCursorParser;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.type.IncomeConditionType;
import com.chungbazi.server.domain.policy.domain.type.PolicySortType;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PolicyPopularityCalculatorTest {

    @Test
    void calculatesPopularityScoreUsingViewCountAndSaveCount() {
        Policy policy = policyWithCounts(12, 3);

        long result = PolicyPopularityCalculator.calculate(policy);

        assertThat(result).isEqualTo(27);
    }

    @Test
    void popularCursorUsesPopularityCalculatorScore() {
        Policy policy = policyWithCounts(12, 3);
        ReflectionTestUtils.setField(policy, "id", 10L);

        String encodedCursor = PolicyCursorParser.encode(PolicySortType.POPULAR, policy);
        PolicyCursor decodedCursor = PolicyCursorParser.decode(encodedCursor, PolicySortType.POPULAR);

        assertThat(decodedCursor.popularityScore()).isEqualTo(PolicyPopularityCalculator.calculate(policy));
        assertThat(decodedCursor.registeredAt()).isEqualTo(policy.getRegisteredAt());
        assertThat(decodedCursor.policyId()).isEqualTo(10L);
    }

    private Policy policyWithCounts(int viewCount, int saveCount) {
        Policy policy = Policy.createPolicy(
                "P001",
                "테스트 정책",
                null,
                null,
                null,
                PolicySubCategoryType.EMPLOYMENT_PREPARATION,
                true,
                null,
                null,
                null,
                RecruitmentType.ALWAYS,
                RecruitmentStatus.OPEN,
                null,
                null,
                null,
                null,
                IncomeConditionType.NO_LIMIT,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
        ReflectionTestUtils.setField(policy, "viewCount", viewCount);
        ReflectionTestUtils.setField(policy, "saveCount", saveCount);
        return policy;
    }
}
