package com.chungbazi.server.domain.policy.application.support;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.type.IncomeConditionType;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.type.IncomeLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PolicyIncomeMatcherTest {

    private PolicyIncomeMatcher incomeMatcher;

    @BeforeEach
    void setUp() {
        incomeMatcher = new PolicyIncomeMatcher();
    }

    @Test
    @DisplayName("사용자 소득분위가 정책 최대 분위 이하이면 일치한다")
    void matchesIncomeLevel() {
        User user = mock(User.class);
        Policy policy = mock(Policy.class);

        when(user.getIncomeLevel())
                .thenReturn(IncomeLevel.LEVEL_4);

        when(policy.getIncomeConditionType())
                .thenReturn(IncomeConditionType.OTHER);

        when(policy.getIncomeDescription())
                .thenReturn("소득 6분위 이하");

        IncomeMatchResult result = incomeMatcher.match(user, policy);

        assertThat(result).isEqualTo(IncomeMatchResult.MATCH);
    }

    @Test
    @DisplayName("사용자 소득분위가 정책 최대 분위보다 높으면 불일치한다")
    void mismatchesIncomeLevel() {
        User user = mock(User.class);
        Policy policy = mock(Policy.class);

        when(user.getIncomeLevel())
                .thenReturn(IncomeLevel.LEVEL_8);

        when(policy.getIncomeConditionType())
                .thenReturn(IncomeConditionType.OTHER);

        when(policy.getIncomeDescription())
                .thenReturn("소득 6분위 이하");

        IncomeMatchResult result = incomeMatcher.match(user, policy);

        assertThat(result).isEqualTo(IncomeMatchResult.MISMATCH);
    }

    @Test
    @DisplayName("사용자와 정책의 소득분위가 경계값에서 같으면 일치한다")
    void matchesAtIncomeBoundary() {
        User user = mock(User.class);
        Policy policy = mock(Policy.class);

        when(user.getIncomeLevel())
                .thenReturn(IncomeLevel.LEVEL_6);

        when(policy.getIncomeConditionType())
                .thenReturn(IncomeConditionType.OTHER);

        when(policy.getIncomeDescription())
                .thenReturn("소득 6분위 이하");

        IncomeMatchResult result = incomeMatcher.match(user, policy);

        assertThat(result).isEqualTo(IncomeMatchResult.MATCH);
    }

    @Test
    @DisplayName("중위소득 비율 조건은 소득분위와 비교하지 않는다")
    void returnsUnknownForMedianIncomeCondition() {
        User user = mock(User.class);
        Policy policy = mock(Policy.class);

        when(user.getIncomeLevel())
                .thenReturn(IncomeLevel.LEVEL_4);

        when(policy.getIncomeConditionType())
                .thenReturn(IncomeConditionType.OTHER);

        when(policy.getIncomeDescription())
                .thenReturn("기준 중위소득 120% 이하");

        IncomeMatchResult result = incomeMatcher.match(user, policy);

        assertThat(result).isEqualTo(IncomeMatchResult.UNKNOWN);
    }

    @Test
    @DisplayName("여러 소득분위 조건이 포함되면 판단하지 않는다")
    void returnsUnknownForMultipleConditions() {
        User user = mock(User.class);
        Policy policy = mock(Policy.class);

        when(user.getIncomeLevel())
                .thenReturn(IncomeLevel.LEVEL_4);

        when(policy.getIncomeConditionType())
                .thenReturn(IncomeConditionType.OTHER);

        when(policy.getIncomeDescription())
                .thenReturn("Ⅰ유형 6분위 이하, Ⅱ유형 9분위 이하");

        IncomeMatchResult result = incomeMatcher.match(user, policy);

        assertThat(result).isEqualTo(IncomeMatchResult.UNKNOWN);
    }

    @Test
    @DisplayName("사용자의 소득분위가 UNKNOWN이면 판단하지 않는다")
    void returnsUnknownWhenUserIncomeIsUnknown() {
        User user = mock(User.class);
        Policy policy = mock(Policy.class);

        when(user.getIncomeLevel())
                .thenReturn(IncomeLevel.UNKNOWN);

        IncomeMatchResult result = incomeMatcher.match(user, policy);

        assertThat(result).isEqualTo(IncomeMatchResult.UNKNOWN);
    }
}
