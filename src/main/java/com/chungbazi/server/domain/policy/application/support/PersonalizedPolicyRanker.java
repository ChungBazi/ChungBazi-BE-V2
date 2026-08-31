package com.chungbazi.server.domain.policy.application.support;

import com.chungbazi.server.domain.policy.application.dto.PolicyRecommendationContext;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.user.domain.User;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PersonalizedPolicyRanker {

    private static final int MAX_SAME_CATEGORY_COUNT = 3;

    private final PersonalizedPolicyScorer scorer;

    public List<Policy> rank(
            User user,
            PolicyRecommendationContext context,
            List<Policy> candidates,
            int size
    ) {
        return rankByMinimumScore(user, context, candidates, size, Integer.MIN_VALUE);
    }

    public List<Policy> rankMatched(
            User user,
            PolicyRecommendationContext context,
            List<Policy> candidates,
            int size
    ) {
        return rankByMinimumScore(user, context, candidates, size, 1);
    }

    private List<Policy> rankByMinimumScore(
            User user,
            PolicyRecommendationContext context,
            List<Policy> candidates,
            int size,
            int minimumScore
    ) {
        List<Policy> rankedPolicies = candidates.stream()
                .filter(policy -> scorer.isEligible(user, policy))
                .filter(policy -> scorer.score(user, context, policy) >= minimumScore)
                .sorted(Comparator
                        .comparingInt((Policy policy) -> scorer.score(user, context, policy))
                        .reversed()
                        .thenComparing(Policy::getRegisteredAt, Comparator.reverseOrder()))
                .toList();

        if (context.interestCategoryCounts().size() <= 1) {
            return rankedPolicies.stream()
                    .limit(size)
                    .toList();
        }
        return diversifyByCategory(rankedPolicies, size);
    }

    private List<Policy> diversifyByCategory(List<Policy> policies, int size) {
        List<Policy> selectedPolicies = new ArrayList<>();
        Map<PolicyCategoryType, Integer> selectedCategoryCounts =
                new EnumMap<>(PolicyCategoryType.class);

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
