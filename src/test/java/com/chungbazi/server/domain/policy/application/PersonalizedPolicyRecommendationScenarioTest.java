package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.application.support.PersonalizedPolicyScorer;
import com.chungbazi.server.domain.policy.application.support.PersonalizedPolicyRanker;
import com.chungbazi.server.domain.policy.application.support.PolicyIncomeMatcher;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.repository.RecentViewedPolicyRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.IncomeConditionType;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.UserInterest;
import com.chungbazi.server.domain.user.domain.type.IncomeLevel;
import com.chungbazi.server.domain.user.infrastructure.UserInterestRepository;
import com.chungbazi.server.fixture.PolicyFixture;
import com.chungbazi.server.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PersonalizedPolicyRecommendationScenarioTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private UserInterestRepository userInterestRepository;

    @Mock
    private PolicyLikeRepository policyLikeRepository;

    @Mock
    private RecentViewedPolicyRepository recentViewedPolicyRepository;

    private PersonalizedPolicyService service;

    @BeforeEach
    void setUp() {
        PolicyIncomeMatcher incomeMatcher = new PolicyIncomeMatcher();

        PersonalizedPolicyScorer scorer = new PersonalizedPolicyScorer(incomeMatcher);

        service = new PersonalizedPolicyService(
                policyRepository,
                userInterestRepository,
                policyLikeRepository,
                recentViewedPolicyRepository,
                new PersonalizedPolicyRanker(scorer)
        );
    }

    @Test
    @DisplayName("취업 관심 4분위 사용자에게 적합한 정책을 우선 추천한다")
    void recommendsSuitablePoliciesForJobSeeker() {
        // given
        User user = UserFixture.user()
                .id(1L)
                .age(25)
                .incomeLevel(IncomeLevel.LEVEL_4)
                .build();

        /*
         * 관심사와 소득 조건 모두 일치
         *
         * 관심 소분류  +35
         * 관심 대분류 개수  +10
         * 소득분위 일치  +10
         * 총점  55
         */
        Policy suitableIncomePolicy = PolicyFixture.policy()
                .id(1L)
                .title("청년 취업 지원")
                .subCategory(PolicySubCategoryType.EMPLOYMENT_PREPARATION)
                .age(18, 34)
                .income(IncomeConditionType.OTHER, "소득 6분위 이하")
                .registeredAt(LocalDateTime.of(2026, 8, 1, 0, 0))
                .build();

        /*
         * 관심사는 일치하지만 소득 조건은 불일치
         *
         * 관심 소분류  +35
         * 관심 대분류 개수  +10
         * 소득 가산점  0
         * 총점  45
         */
        Policy mismatchedIncomePolicy = PolicyFixture.policy()
                .id(2L)
                .title("저소득 청년 취업 지원")
                .subCategory(PolicySubCategoryType.EMPLOYMENT_PREPARATION)
                .age(18, 34)
                .income(IncomeConditionType.OTHER, "소득 3분위 이하")
                .registeredAt(LocalDateTime.of(2026, 8, 5, 0, 0))
                .build();

        /*
         * 동일 대분류지만 관심 소분류는 아님
         *
         * 관심 대분류 개수  +10
         */
        Policy sameCategoryPolicy = PolicyFixture.policy()
                .id(3L)
                .title("청년 재직 지원")
                .subCategory(PolicySubCategoryType.WORK_LIFE)
                .age(18, 34)
                .registeredAt(LocalDateTime.of(2026, 8, 4, 0, 0))
                .build();

        // 관심 분야와 관계없는 정책
        Policy unrelatedPolicy = PolicyFixture.policy()
                .id(4L)
                .title("청년 주거 지원")
                .subCategory(PolicySubCategoryType.HOUSING_COST_SPACE)
                .age(18, 34)
                .registeredAt(LocalDateTime.of(2026, 8, 3, 0, 0))
                .build();

        // 관심사와 소득은 일치하지만 사용자 나이가 대상 범위 밖인 경우, 추천 결과에서 제외되어야 한다.
        Policy ageIneligiblePolicy = PolicyFixture.policy()
                .id(5L)
                .title("중장년 취업 지원")
                .subCategory(PolicySubCategoryType.EMPLOYMENT_PREPARATION)
                .age(30, 39)
                .income(IncomeConditionType.OTHER, "소득 6분위 이하")
                .registeredAt(LocalDateTime.of(2026, 8, 6, 0, 0))
                .build();

        List<Policy> candidates = List.of(
                unrelatedPolicy,
                ageIneligiblePolicy,
                sameCategoryPolicy,
                mismatchedIncomePolicy,
                suitableIncomePolicy
        );

        UserInterest interest = UserInterest.createUserInterest(
                user,
                PolicySubCategoryType.EMPLOYMENT_PREPARATION
        );

        prepareRepositories(
                user,
                candidates,
                List.of(interest)
        );

        // when
        List<Policy> result =
                service.getPersonalizedPolicyEntities(user, 5);

        // then
        assertThat(result)
                .extracting(Policy::getId)
                .containsExactly(
                        1L, // 관심사 + 소득 일치
                        2L, // 관심사 일치, 소득 불일치
                        3L, // 같은 관심 대분류
                        4L  // 관심 분야와 무관
                );

        assertThat(result)
                .extracting(Policy::getId)
                .doesNotContain(5L);
    }

    @Test
    @DisplayName("소득분위가 UNKNOWN이면 소득 조건이 추천 순위에 영향을 주지 않는다")
    void ignoresIncomeConditionWhenUserIncomeIsUnknown() {
        // given
        User user = UserFixture.user()
                .id(1L)
                .age(25)
                .incomeLevel(IncomeLevel.UNKNOWN)
                .build();

        Policy olderIncomePolicy = PolicyFixture.policy()
                .id(1L)
                .title("소득 조건 취업 정책")
                .subCategory(PolicySubCategoryType.EMPLOYMENT_PREPARATION)
                .age(18, 34)
                .income(IncomeConditionType.OTHER, "소득 6분위 이하")
                .registeredAt(LocalDateTime.of(2026, 8, 1, 0, 0))
                .build();

        Policy newerNoLimitPolicy = PolicyFixture.policy()
                .id(2L)
                .title("소득 제한 없는 취업 정책")
                .subCategory(PolicySubCategoryType.EMPLOYMENT_PREPARATION)
                .age(18, 34)
                .registeredAt(LocalDateTime.of(2026, 8, 5, 0, 0))
                .build();

        UserInterest interest = UserInterest.createUserInterest(
                user,
                PolicySubCategoryType.EMPLOYMENT_PREPARATION
        );

        prepareRepositories(
                user,
                List.of(
                        olderIncomePolicy,
                        newerNoLimitPolicy
                ),
                List.of(interest)
        );

        // when
        List<Policy> result = service.getPersonalizedPolicyEntities(user, 2);

        // then
        // 두 정책의 관심사 점수가 같고 소득 점수도 없으므로, 등록일이 최신인 정책이 먼저 나온다.
        assertThat(result)
                .extracting(Policy::getId)
                .containsExactly(2L, 1L);
    }

    private void prepareRepositories(
            User user,
            List<Policy> candidates,
            List<UserInterest> interests
    ) {
        when(policyRepository.findAllLatestPolicies(
                eq(RecruitmentStatus.CLOSED),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(candidates);

        when(userInterestRepository.findAllByUser(user))
                .thenReturn(interests);

        when(policyLikeRepository.findRecentPolicyLikesWithPolicy(
                eq(1L),
                any(Pageable.class)
        )).thenReturn(List.of());

        when(recentViewedPolicyRepository.findRecentViewedPolicies(
                eq(1L),
                eq(RecruitmentStatus.CLOSED),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(List.of());
    }

}
