package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.application.dto.PolicyRecommendationContext;
import com.chungbazi.server.domain.policy.application.support.PersonalizedPolicyScorer;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.repository.RecentViewedPolicyRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.infrastructure.UserInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalizedPolicyService {

    private static final int CANDIDATE_SIZE = 100;
    private static final int BEHAVIOR_HISTORY_SIZE = 50;
    private static final int MAX_SAME_CATEGORY_COUNT = 2;

    private final PolicyRepository policyRepository;
    private final UserInterestRepository userInterestRepository;
    private final PolicyLikeRepository policyLikeRepository;
    private final RecentViewedPolicyRepository recentViewedPolicyRepository;
    private final PersonalizedPolicyScorer scorer;

    public List<Policy> getPersonalizedPolicyEntities(User user, int size) {
        List<Policy> candidates = policyRepository.findAllLatestPolicies(
                RecruitmentStatus.CLOSED,
                user.getSidoCode(),
                user.getSigunguCode(),
                PageRequest.of(0, CANDIDATE_SIZE)
        );

        PolicyRecommendationContext context = PolicyRecommendationContext.of(
                userInterestRepository.findAllByUser(user),
                policyLikeRepository.findRecentPolicyLikesWithPolicy(
                        user.getId(),
                        PageRequest.of(0, BEHAVIOR_HISTORY_SIZE)
                ),
                recentViewedPolicyRepository.findRecentViewedPolicies(
                        user.getId(),
                        RecruitmentStatus.CLOSED,
                        user.getSidoCode(),
                        user.getSigunguCode(),
                        PageRequest.of(0, BEHAVIOR_HISTORY_SIZE)
                )
        );

        List<Policy> scoredPolicies = candidates.stream()
                .filter(policy -> scorer.isEligible(user, policy))
                .sorted(Comparator
                        .comparingInt((Policy policy) -> scorer.score(user, context, policy))
                        .reversed()
                        .thenComparing(Policy::getRegisteredAt, Comparator.reverseOrder()))
                .toList();

        return diversifyByCategory(scoredPolicies, size);
    }

    private List<Policy> diversifyByCategory(List<Policy> policies, int size) {
        List<Policy> selectedPolicies = new ArrayList<>();
        Map<PolicyCategoryType, Integer> selectedCategoryCounts = new EnumMap<>(PolicyCategoryType.class);

        for (Policy policy : policies) {
            PolicyCategoryType category = policy.getCategory();
            int categoryCount = selectedCategoryCounts.getOrDefault(category, 0);

            if (categoryCount >= MAX_SAME_CATEGORY_COUNT) {
                continue;
            }
            selectedPolicies.add(policy);
            selectedCategoryCounts.put(category, categoryCount + 1);

            if (selectedPolicies.size() == size) {
                return selectedPolicies;
            }
        }

        for (Policy policy : policies) {
            if (selectedPolicies.contains(policy)) {
                continue;
            }
            selectedPolicies.add(policy);

            if (selectedPolicies.size() == size) {
                return selectedPolicies;
            }
        }
        return selectedPolicies;
    }
}
