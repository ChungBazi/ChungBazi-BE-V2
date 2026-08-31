package com.chungbazi.server.domain.policy.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicyDisplayStatus;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentType;
import com.chungbazi.server.fixture.PolicyFixture;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "firebase.enabled=false")
@ActiveProfiles("test")
@Transactional
class PolicyVisibilityRepositoryTest {

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PolicyLikeRepository policyLikeRepository;

    @Test
    void excludesHiddenPoliciesFromPolicyListQueries() {
        Policy visiblePolicy = policy("visible-list-policy", PolicyDisplayStatus.VISIBLE);
        Policy hiddenPolicy = policy("hidden-list-policy", PolicyDisplayStatus.HIDDEN_EXPIRED);
        policyRepository.saveAllAndFlush(List.of(visiblePolicy, hiddenPolicy));

        List<Policy> policies = policyRepository.findAllLatestPolicies(
                RecruitmentStatus.CLOSED,
                null,
                null,
                PageRequest.of(0, 10)
        );

        assertThat(policies)
                .extracting(Policy::getPlcyNo)
                .contains("visible-list-policy")
                .doesNotContain("hidden-list-policy");
    }

    @Test
    void excludesHiddenPoliciesFromLikedPolicyQueries() {
        Policy visiblePolicy = policy("visible-liked-policy", PolicyDisplayStatus.VISIBLE);
        Policy hiddenPolicy = policy("hidden-liked-policy", PolicyDisplayStatus.HIDDEN_EXPIRED);
        policyRepository.saveAllAndFlush(List.of(visiblePolicy, hiddenPolicy));
        policyLikeRepository.saveAllAndFlush(List.of(
                PolicyLike.createPolicyLike(1L, visiblePolicy, null),
                PolicyLike.createPolicyLike(1L, hiddenPolicy, null)
        ));

        List<Policy> policies = policyLikeRepository.findMyLikedPoliciesOrderByLatestFirst(
                1L,
                RecruitmentStatus.CLOSED,
                null,
                PageRequest.of(0, 10)
        );

        assertThat(policies)
                .extracting(Policy::getPlcyNo)
                .containsExactly("visible-liked-policy");
    }

    @Test
    void doesNotIncreaseViewCountForHiddenPolicy() {
        Policy hiddenPolicy = policy("hidden-detail-policy", PolicyDisplayStatus.HIDDEN_EXPIRED);
        policyRepository.saveAndFlush(hiddenPolicy);

        int updatedCount = policyRepository.increaseViewCount(hiddenPolicy.getId());

        assertThat(updatedCount).isZero();
    }

    @Test
    void findsUpcomingDeadlinePoliciesOnlyUntilFiftyDays() {
        LocalDate today = LocalDate.of(2026, 8, 31);
        LocalDate deadlineUntil = today.plusDays(50);
        Policy deadlineTodayPolicy = policyWithApplyEndDate("deadline-today-policy", today);
        Policy deadlineInFiftyDaysPolicy = policyWithApplyEndDate("deadline-d50-policy", deadlineUntil);
        Policy deadlineInFiftyOneDaysPolicy = policyWithApplyEndDate("deadline-d51-policy", today.plusDays(51));
        Policy expiredPolicy = policyWithApplyEndDate("expired-policy", today.minusDays(1));
        Policy openEndedPolicy = policyWithApplyEndDate("open-ended-policy", null);
        policyRepository.saveAllAndFlush(List.of(
                deadlineTodayPolicy,
                deadlineInFiftyDaysPolicy,
                deadlineInFiftyOneDaysPolicy,
                expiredPolicy,
                openEndedPolicy
        ));

        List<Policy> policies = policyRepository.findAllUpcomingDeadlinePolicies(
                RecruitmentStatus.CLOSED,
                today,
                deadlineUntil,
                null,
                null,
                PageRequest.of(0, 10)
        );
        long count = policyRepository.countVisibleUpcomingDeadlinePolicies(
                RecruitmentStatus.CLOSED,
                today,
                deadlineUntil,
                null,
                null
        );

        assertThat(policies)
                .extracting(Policy::getPlcyNo)
                .containsExactly("deadline-today-policy", "deadline-d50-policy");
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void findsLikedPoliciesDeadlineWithinTwoWeeks() {
        Long userId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 8, 31);
        LocalDate deadlineUntil = targetDate.plusDays(14);
        Policy deadlineTodayPolicy = fixedPeriodPolicy("liked-deadline-today-policy", targetDate);
        Policy deadlineInFourteenDaysPolicy = fixedPeriodPolicy("liked-deadline-d14-policy", deadlineUntil);
        Policy deadlineInFifteenDaysPolicy = fixedPeriodPolicy("liked-deadline-d15-policy", targetDate.plusDays(15));
        Policy openEndedPolicy = PolicyFixture.policy()
                .id(null)
                .policyNumber("liked-open-ended-policy")
                .applyEndDate(targetDate.plusDays(1))
                .recruitmentType(RecruitmentType.ALWAYS)
                .build();
        Policy hiddenPolicy = PolicyFixture.policy()
                .id(null)
                .policyNumber("liked-hidden-deadline-policy")
                .applyEndDate(targetDate.plusDays(2))
                .recruitmentType(RecruitmentType.FIXED_PERIOD)
                .displayStatus(PolicyDisplayStatus.HIDDEN_EXPIRED)
                .build();
        Policy notLikedPolicy = fixedPeriodPolicy("not-liked-deadline-policy", targetDate.plusDays(3));
        policyRepository.saveAllAndFlush(List.of(
                deadlineTodayPolicy,
                deadlineInFourteenDaysPolicy,
                deadlineInFifteenDaysPolicy,
                openEndedPolicy,
                hiddenPolicy,
                notLikedPolicy
        ));
        policyLikeRepository.saveAllAndFlush(List.of(
                PolicyLike.createPolicyLike(userId, deadlineInFourteenDaysPolicy, null),
                PolicyLike.createPolicyLike(userId, hiddenPolicy, null),
                PolicyLike.createPolicyLike(userId, openEndedPolicy, null),
                PolicyLike.createPolicyLike(userId, deadlineInFifteenDaysPolicy, null),
                PolicyLike.createPolicyLike(userId, deadlineTodayPolicy, null)
        ));

        List<Policy> policies = policyLikeRepository.findUpcomingDeadlineLikedPoliciesWithinPeriod(
                userId,
                RecruitmentStatus.CLOSED,
                RecruitmentType.ALWAYS,
                targetDate,
                deadlineUntil
        );

        assertThat(policies)
                .extracting(Policy::getPlcyNo)
                .containsExactly("liked-deadline-today-policy", "liked-deadline-d14-policy");
    }

    private Policy policy(String policyNumber, PolicyDisplayStatus displayStatus) {
        return PolicyFixture.policy()
                .id(null)
                .policyNumber(policyNumber)
                .displayStatus(displayStatus)
                .build();
    }

    private Policy policyWithApplyEndDate(String policyNumber, LocalDate applyEndDate) {
        return PolicyFixture.policy()
                .id(null)
                .policyNumber(policyNumber)
                .applyEndDate(applyEndDate)
                .build();
    }

    private Policy fixedPeriodPolicy(String policyNumber, LocalDate applyEndDate) {
        return PolicyFixture.policy()
                .id(null)
                .policyNumber(policyNumber)
                .applyEndDate(applyEndDate)
                .recruitmentType(RecruitmentType.FIXED_PERIOD)
                .build();
    }
}
