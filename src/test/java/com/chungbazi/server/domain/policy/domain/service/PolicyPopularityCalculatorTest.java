package com.chungbazi.server.domain.policy.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.chungbazi.server.domain.policy.application.PolicyPopularityCalculator;
import com.chungbazi.server.domain.policy.application.cursor.PolicyCursor;
import com.chungbazi.server.domain.policy.application.cursor.PolicyCursorParser;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.type.PolicySortType;
import com.chungbazi.server.fixture.PolicyFixture;
import org.junit.jupiter.api.Test;

class PolicyPopularityCalculatorTest {

    @Test
    void calculatesPopularityScoreUsingViewCountAndSaveCount() {
        Policy policy = PolicyFixture.policy()
                .counts(12, 3)
                .build();

        long result = PolicyPopularityCalculator.calculate(policy);

        assertThat(result).isEqualTo(27);
    }

    @Test
    void popularCursorUsesPopularityCalculatorScore() {
        Policy policy = PolicyFixture.policy()
                .id(10L)
                .counts(12, 3)
                .build();

        String encodedCursor = PolicyCursorParser.encode(PolicySortType.POPULAR, policy);
        PolicyCursor decodedCursor = PolicyCursorParser.decode(encodedCursor, PolicySortType.POPULAR);

        assertThat(decodedCursor.popularityScore()).isEqualTo(PolicyPopularityCalculator.calculate(policy));
        assertThat(decodedCursor.registeredAt()).isEqualTo(policy.getRegisteredAt());
        assertThat(decodedCursor.policyId()).isEqualTo(10L);
    }
}
