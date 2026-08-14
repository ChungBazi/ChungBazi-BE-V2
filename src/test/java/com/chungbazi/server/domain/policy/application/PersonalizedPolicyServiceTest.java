package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.application.dto.PolicyRecommendationContext;
import com.chungbazi.server.domain.policy.application.support.PersonalizedPolicyScorer;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.repository.RecentViewedPolicyRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.IncomeConditionType;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentType;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.UserInterest;
import com.chungbazi.server.domain.user.infrastructure.UserInterestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PersonalizedPolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private UserInterestRepository userInterestRepository;

    @Mock
    private PolicyLikeRepository policyLikeRepository;

    @Mock
    private RecentViewedPolicyRepository recentViewedPolicyRepository;

    @Mock
    private PersonalizedPolicyScorer scorer;

    private PersonalizedPolicyService service;

    @BeforeEach
    void setUp() {
        service = new PersonalizedPolicyService(
                policyRepository,
                userInterestRepository,
                policyLikeRepository,
                recentViewedPolicyRepository,
                scorer
        );
    }

    @Test
    @DisplayName("추천 점수 내림차순으로 정렬하고 동점이면 최신 정책을 우선한다")
    void sortsByScoreAndRegisteredAt() {
        User user = mock(User.class);

        Policy highest = policy(
                1L,
                PolicySubCategoryType.EMPLOYMENT_PREPARATION,
                LocalDateTime.of(2026, 8, 1, 0, 0)
        );

        Policy recentTie = policy(
                2L,
                PolicySubCategoryType.WORK_LIFE,
                LocalDateTime.of(2026, 8, 10, 0, 0)
        );

        Policy oldTie = policy(
                3L,
                PolicySubCategoryType.STARTUP_BUSINESS,
                LocalDateTime.of(2026, 7, 1, 0, 0)
        );

        prepareRecommendationData(
                user,
                List.of(oldTie, recentTie, highest),
                List.of()
        );

        when(scorer.isEligible(eq(user), any(Policy.class)))
                .thenReturn(true);

        when(scorer.score(eq(user), any(PolicyRecommendationContext.class), eq(highest)))
                .thenReturn(100);

        when(scorer.score(eq(user), any(PolicyRecommendationContext.class), eq(recentTie)))
                .thenReturn(50);

        when(scorer.score(eq(user), any(PolicyRecommendationContext.class), eq(oldTie)))
                .thenReturn(50);

        List<Policy> result = service.getPersonalizedPolicyEntities(user, 3);

        assertThat(result)
                .extracting(Policy::getId)
                .containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("관심 대분류가 여러 개면 동일 카테고리를 우선 최대 3개까지만 노출한다")
    void diversifiesPoliciesByCategory() {
        User user = mock(User.class);

        Policy job1 = policy(
                1L,
                PolicySubCategoryType.EMPLOYMENT_PREPARATION,
                LocalDateTime.of(2026, 8, 10, 0, 0)
        );
        Policy job2 = policy(
                2L,
                PolicySubCategoryType.WORK_LIFE,
                LocalDateTime.of(2026, 8, 9, 0, 0)
        );
        Policy job3 = policy(
                3L,
                PolicySubCategoryType.STARTUP_BUSINESS,
                LocalDateTime.of(2026, 8, 8, 0, 0)
        );
        Policy job4 = policy(
                4L,
                PolicySubCategoryType.EMPLOYMENT_PREPARATION,
                LocalDateTime.of(2026, 8, 7, 0, 0)
        );
        Policy housing = policy(
                5L,
                PolicySubCategoryType.HOUSING_COST_SPACE,
                LocalDateTime.of(2026, 8, 6, 0, 0)
        );

        UserInterest jobInterest = mock(UserInterest.class);
        UserInterest housingInterest = mock(UserInterest.class);

        when(jobInterest.getSubCategory())
                .thenReturn(PolicySubCategoryType.EMPLOYMENT_PREPARATION);

        when(housingInterest.getSubCategory())
                .thenReturn(PolicySubCategoryType.HOUSING_COST_SPACE);

        prepareRecommendationData(
                user,
                List.of(job1, job2, job3, job4, housing),
                List.of(jobInterest, housingInterest)
        );

        when(scorer.isEligible(eq(user), any(Policy.class)))
                .thenReturn(true);

        when(scorer.score(
                eq(user),
                any(PolicyRecommendationContext.class),
                any(Policy.class)
        )).thenAnswer(invocation -> {
            Policy policy = invocation.getArgument(2);
            return switch (policy.getId().intValue()) {
                case 1 -> 100;
                case 2 -> 90;
                case 3 -> 80;
                case 4 -> 70;
                case 5 -> 60;
                default -> 0;
            };
        });

        List<Policy> result = service.getPersonalizedPolicyEntities(user, 4);

        // job4가 housing보다 점수는 높지만, 동일 카테고리 우선 노출 제한이 3개이므로 housing이 선택된다.
        assertThat(result)
                .extracting(Policy::getId)
                .containsExactly(1L, 2L, 3L, 5L);
    }

    private void prepareRecommendationData(
            User user,
            List<Policy> candidates,
            List<UserInterest> interests
    ) {
        when(policyRepository.findAllLatestPolicies(
                any(),
                any(),
                any(),
                any(Pageable.class)
        )).thenReturn(candidates);

        when(userInterestRepository.findAllByUser(user))
                .thenReturn(interests);

        when(policyLikeRepository.findRecentPolicyLikesWithPolicy(
                any(),
                any(Pageable.class)
        )).thenReturn(List.of());

        when(recentViewedPolicyRepository.findRecentViewedPolicies(
                any(),
                any(),
                any(),
                any(),
                any(Pageable.class)
        )).thenReturn(List.of());
    }

    private Policy policy(Long id, PolicySubCategoryType subCategory, LocalDateTime registeredAt) {
        Policy policy = Policy.createPolicy(
                "P-" + id,
                "테스트 정책 " + id,
                null,
                null,
                null,
                subCategory,
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
                registeredAt,
                registeredAt
        );
        ReflectionTestUtils.setField(policy, "id", id);
        return policy;
    }
}
