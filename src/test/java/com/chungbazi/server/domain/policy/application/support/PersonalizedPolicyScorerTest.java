package com.chungbazi.server.domain.policy.application.support;

import com.chungbazi.server.domain.policy.application.dto.PolicyRecommendationContext;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonalizedPolicyScorerTest {

    private PersonalizedPolicyScorer scorer;

    @BeforeEach
    void setUp() {
        scorer = new PersonalizedPolicyScorer(new PolicyIncomeMatcher());
    }

    @Test
    @DisplayName("관심사, 찜, 최근 조회 이력을 추천 점수에 반영한다")
    void calculatesScoreFromUserBehavior() {
        User user = mock(User.class);
        Policy policy = mock(Policy.class);

        when(policy.getId()).thenReturn(1L);
        when(policy.getCategory())
                .thenReturn(PolicyCategoryType.JOB_STARTUP);
        when(policy.getSubCategory())
                .thenReturn(PolicySubCategoryType.EMPLOYMENT_PREPARATION);

        PolicyRecommendationContext context =
                PolicyRecommendationContext.builder()
                        .interestSubCategories(Set.of(
                                PolicySubCategoryType.EMPLOYMENT_PREPARATION
                        ))
                        .interestCategoryCounts(Map.of(
                                PolicyCategoryType.JOB_STARTUP,
                                1L
                        ))
                        .likedSubCategoryAffinities(Map.of(
                                PolicySubCategoryType.EMPLOYMENT_PREPARATION,
                                1.0
                        ))
                        .recentViewedSubCategoryAffinities(Map.of(
                                PolicySubCategoryType.EMPLOYMENT_PREPARATION,
                                1.0
                        ))
                        .recentViewedPolicyIds(Set.of(1L))
                        .build();

        int result = scorer.score(user, context, policy);

        /*
         * 관심 소분류  +35
         * 관심 대분류 개수 1개  +10
         * 찜한 소분류  +10
         * 최근 조회한 소분류  +3
         * 이미 조회한 동일 정책  -15
         * 합계  43
         */
        assertThat(result).isEqualTo(43);
    }

    @Test
    @DisplayName("최근 검색어 관련도 점수를 추천 점수에 반영한다")
    void addsRecentSearchScore() {
        User user = mock(User.class);
        Policy policy = mock(Policy.class);

        when(policy.getId()).thenReturn(1L);
        when(policy.getCategory()).thenReturn(PolicyCategoryType.JOB_STARTUP);
        when(policy.getSubCategory()).thenReturn(
                PolicySubCategoryType.EMPLOYMENT_PREPARATION
        );

        PolicyRecommendationContext context = PolicyRecommendationContext.builder()
                .interestSubCategories(Set.of())
                .interestCategoryCounts(Map.of())
                .likedSubCategoryAffinities(Map.of())
                .recentViewedSubCategoryAffinities(Map.of())
                .recentViewedPolicyIds(Set.of())
                .recentSearchScores(Map.of(1L, 12))
                .build();

        assertThat(scorer.score(user, context, policy)).isEqualTo(12);
    }

    @Test
    @DisplayName("정책의 연령 조건을 만족하지 않으면 추천 대상에서 제외한다")
    void excludesPolicyWhenAgeDoesNotMatch() {
        User user = mock(User.class);
        Policy policy = mock(Policy.class);

        when(user.getAge(any(LocalDate.class))).thenReturn(18);
        when(policy.getMinAge()).thenReturn(19);
        when(policy.getMaxAge()).thenReturn(34);

        boolean result = scorer.isEligible(user, policy);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자 나이가 정책의 최소·최대 연령과 같으면 추천 대상에 포함한다")
    void includesPolicyAtAgeBoundary() {
        User user = mock(User.class);
        Policy policy = mock(Policy.class);

        when(policy.getMinAge()).thenReturn(19);
        when(policy.getMaxAge()).thenReturn(34);

        // 최소 연령 경계
        when(user.getAge(any(LocalDate.class))).thenReturn(19);

        assertThat(scorer.isEligible(user, policy)).isTrue();

        // 최대 연령 경계
        when(user.getAge(any(LocalDate.class))).thenReturn(34);

        assertThat(scorer.isEligible(user, policy)).isTrue();
    }
}
