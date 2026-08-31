package com.chungbazi.server.domain.policy.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.IncomeConditionType;
import com.chungbazi.server.domain.policy.domain.type.PolicyDisplayStatus;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
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

    private Policy policy(String policyNumber, PolicyDisplayStatus displayStatus) {
        Policy policy = Policy.createPolicy(
                policyNumber,
                "테스트 정책",
                "요약",
                "지원 내용",
                "https://example.com",
                PolicySubCategoryType.EMPLOYMENT_PREPARATION,
                true,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                "2026.01.01 ~ 2026.12.31",
                RecruitmentType.FIXED_PERIOD,
                RecruitmentStatus.OPEN,
                null,
                null,
                null,
                null,
                IncomeConditionType.NO_LIMIT,
                null,
                null,
                null,
                "테스트 기관",
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
        ReflectionTestUtils.setField(policy, "displayStatus", displayStatus);
        return policy;
    }
}
