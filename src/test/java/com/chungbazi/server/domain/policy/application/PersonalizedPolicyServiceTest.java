package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.application.dto.PolicyRecommendationContext;
import com.chungbazi.server.domain.policy.application.support.PersonalizedPolicyRanker;
import com.chungbazi.server.domain.policy.application.support.PersonalizedPolicyScorer;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.repository.RecentViewedPolicyRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.*;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.UserInterest;
import com.chungbazi.server.domain.user.domain.UserSpecialEligibility;
import com.chungbazi.server.domain.user.domain.type.SpecialEligibilityType;
import com.chungbazi.server.domain.user.infrastructure.UserInterestRepository;
import com.chungbazi.server.domain.user.infrastructure.UserSpecialEligibilityRepository;
import com.chungbazi.server.fixture.PolicyFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersonalizedPolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private UserInterestRepository userInterestRepository;

    @Mock
    private UserSpecialEligibilityRepository userSpecialEligibilityRepository;

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
                userSpecialEligibilityRepository,
                policyLikeRepository,
                recentViewedPolicyRepository,
                new PersonalizedPolicyRanker(scorer)
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

        List<Policy> result = service.getPersonalizedPolicies(user, 3);

        assertThat(result)
                .extracting(Policy::getId)
                .containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("추천 점수가 1점 미만인 정책은 추천 결과에서 제외한다")
    void excludesPoliciesBelowMinimumRecommendationScore() {
        User user = mock(User.class);
        Policy matched = policy(
                1L,
                PolicySubCategoryType.EMPLOYMENT_PREPARATION,
                LocalDateTime.of(2026, 8, 3, 0, 0)
        );
        Policy zeroScore = policy(
                2L,
                PolicySubCategoryType.WORK_LIFE,
                LocalDateTime.of(2026, 8, 2, 0, 0)
        );
        Policy negativeScore = policy(
                3L,
                PolicySubCategoryType.STARTUP_BUSINESS,
                LocalDateTime.of(2026, 8, 1, 0, 0)
        );

        prepareRecommendationData(
                user,
                List.of(matched, zeroScore, negativeScore),
                List.of()
        );

        when(scorer.isEligible(eq(user), any(Policy.class)))
                .thenReturn(true);
        when(scorer.score(eq(user), any(PolicyRecommendationContext.class), eq(matched)))
                .thenReturn(1);
        when(scorer.score(eq(user), any(PolicyRecommendationContext.class), eq(zeroScore)))
                .thenReturn(0);
        when(scorer.score(eq(user), any(PolicyRecommendationContext.class), eq(negativeScore)))
                .thenReturn(-1);

        List<Policy> result = service.getPersonalizedPolicies(user, 3);

        assertThat(result)
                .extracting(Policy::getId)
                .containsExactly(1L);
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

        List<Policy> result = service.getPersonalizedPolicies(user, 4);

        // job4가 housing보다 점수는 높지만, 동일 카테고리 우선 노출 제한이 3개이므로 housing이 선택된다.
        assertThat(result)
                .extracting(Policy::getId)
                .containsExactly(1L, 2L, 3L, 5L);
    }

    @Test
    @DisplayName("사용자가 선택하지 않은 카테고리를 조회하면 빈 목록을 반환한다")
    void returnsEmptyWhenCategoryIsNotInterested() {
        User user = mock(User.class);

        UserInterest interest = UserInterest.createUserInterest(
                user,
                PolicySubCategoryType.EMPLOYMENT_PREPARATION
        );

        when(userInterestRepository.findAllByUser(user))
                .thenReturn(List.of(interest));

        List<Policy> result = service.getPersonalizedPoliciesByCategory(
                user,
                PolicyCategoryType.HOUSING,
                5
        );

        assertThat(result).isEmpty();

        // 관심 카테고리가 아니므로 정책 후보 조회도 실행하지 않는다.
        verifyNoInteractions(policyRepository);
    }

    @Test
    @DisplayName("추천 결과는 요청한 개수를 초과하지 않는다")
    void limitsRecommendationResultSize() {
        // given
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Policy policy1 = policy(
                1L,
                PolicySubCategoryType.EMPLOYMENT_PREPARATION,
                LocalDateTime.of(2026, 8, 5, 0, 0)
        );
        Policy policy2 = policy(
                2L,
                PolicySubCategoryType.WORK_LIFE,
                LocalDateTime.of(2026, 8, 4, 0, 0)
        );
        Policy policy3 = policy(
                3L,
                PolicySubCategoryType.STARTUP_BUSINESS,
                LocalDateTime.of(2026, 8, 3, 0, 0)
        );
        Policy policy4 = policy(
                4L,
                PolicySubCategoryType.HOUSING_COST_SPACE,
                LocalDateTime.of(2026, 8, 2, 0, 0)
        );
        Policy policy5 = policy(
                5L,
                PolicySubCategoryType.EDUCATION_COMPETENCY,
                LocalDateTime.of(2026, 8, 1, 0, 0)
        );

        when(policyRepository.findEligiblePolicies(
                isNull(),
                eq(RecruitmentStatus.CLOSED),
                isNull(),
                isNull(),
                eq(Set.of(SpecialEligibilityType.NONE))
        )).thenReturn(List.of(
                policy1,
                policy2,
                policy3,
                policy4,
                policy5
        ));

        when(userInterestRepository.findAllByUser(user))
                .thenReturn(List.of());

        when(userSpecialEligibilityRepository.findAllByUser(user))
                .thenReturn(List.of(UserSpecialEligibility.create(
                        user,
                        SpecialEligibilityType.NONE
                )));

        when(policyLikeRepository.findRecentPolicyLikesWithPolicy(
                eq(1L),
                any(Pageable.class)
        )).thenReturn(List.of());

        when(recentViewedPolicyRepository.findRecentViewedPolicyEvents(
                eq(1L),
                any(Pageable.class)
        )).thenReturn(List.of());

        when(scorer.isEligible(eq(user), any(Policy.class)))
                .thenReturn(true);

        when(scorer.score(
                eq(user),
                any(PolicyRecommendationContext.class),
                any(Policy.class)
        )).thenReturn(10);

        // when
        List<Policy> result =
                service.getPersonalizedPolicies(user, 3);

        // then
        assertThat(result)
                .extracting(Policy::getId)
                .containsExactly(1L, 2L, 3L);
    }

    private void prepareRecommendationData(
            User user,
            List<Policy> candidates,
            List<UserInterest> interests
    ) {
        when(policyRepository.findEligiblePolicies(
                isNull(),
                any(),
                any(),
                any(),
                eq(Set.of(SpecialEligibilityType.NONE))
        )).thenReturn(candidates);

        when(userInterestRepository.findAllByUser(user))
                .thenReturn(interests);

        when(userSpecialEligibilityRepository.findAllByUser(user))
                .thenReturn(List.of(UserSpecialEligibility.create(
                        user,
                        SpecialEligibilityType.NONE
                )));

        when(policyLikeRepository.findRecentPolicyLikesWithPolicy(
                any(),
                any(Pageable.class)
        )).thenReturn(List.of());

        when(recentViewedPolicyRepository.findRecentViewedPolicyEvents(
                any(),
                any(Pageable.class)
        )).thenReturn(List.of());
    }

    private Policy policy(Long id, PolicySubCategoryType subCategory, LocalDateTime registeredAt) {
        return PolicyFixture.policy()
                .id(id)
                .title("테스트 정책 " + id)
                .subCategory(subCategory)
                .registeredAt(registeredAt)
                .build();
    }
}
