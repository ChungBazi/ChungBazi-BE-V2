package com.chungbazi.server.domain.policy.application.support;

import com.chungbazi.server.domain.policy.application.dto.PolicyRecommendationContext;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.user.domain.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
public class PersonalizePolicyScorer {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final int AGE_MATCH_SCORE = 30;
    private static final int INTEREST_SUB_CATEGORY_SCORE = 35;
    private static final int LIKED_SUB_CATEGORY_SCORE = 30;
    private static final int EDUCATION_SCORE = 8;
    private static final int EMPLOYMENT_SCORE = 10;
    private static final int RECENT_VIEWED_SUB_CATEGORY_SCORE = 10;
    private static final int ALREADY_VIEWED_POLICY_PENALTY = -15;

    private final List<RecommendationRule> rules = List.of(
            new RecommendationRule(
                    "AGE_MATCH",
                    AGE_MATCH_SCORE,
                    input -> matchesAge(input.user(), input.policy())
            ),
            new RecommendationRule(
                    "INTEREST_SUB_CATEGORY",
                    INTEREST_SUB_CATEGORY_SCORE,
                    input -> input.context().hasInterestSubCategory(input.policy().getSubCategory())
            ),
            new RecommendationRule(
                    "LIKED_SUB_CATEGORY",
                    LIKED_SUB_CATEGORY_SCORE,
                    input -> input.context().hasLikedSubCategory(input.policy().getSubCategory())
            ),
            new RecommendationRule(
                    "EDUCATION_MATCH",
                    EDUCATION_SCORE,
                    input -> matchesEducation(input.user(), input.policy())
            ),
            new RecommendationRule(
                    "EMPLOYMENT_MATCH",
                    EMPLOYMENT_SCORE,
                    input -> matchesEmployment(input.user(), input.policy())
            ),
            new RecommendationRule(
                    "RECENT_VIEWED_SUB_CATEGORY",
                    RECENT_VIEWED_SUB_CATEGORY_SCORE,
                    input -> input.context().hasRecentlyViewedSubCategory(input.policy().getSubCategory())
            ),
            new RecommendationRule(
                    "ALREADY_VIEWED_POLICY",
                    ALREADY_VIEWED_POLICY_PENALTY,
                    input -> input.context().hasRecentlyViewedPolicy(input.policy().getId())
            )
    );

    public boolean isEligible(User user, Policy policy) {
        return matchesAge(user, policy);
    }

    public int score(User user, PolicyRecommendationContext context, Policy policy) {
        RecommendationInput input = new RecommendationInput(user, context, policy);

        return rules.stream()
                .mapToInt(rule -> rule.evaluate(input))
                .sum();
    }

    private boolean matchesAge(User user, Policy policy) {
        Integer age = user.getAge(LocalDate.now(SERVICE_ZONE_ID));

        if (age == null) {
            return true;
        }

        if (policy.getMinAge() != null && age < policy.getMinAge()) {
            return false;
        }

        return policy.getMaxAge() == null || age <= policy.getMaxAge();
    }

    private boolean matchesEducation(User user, Policy policy) {
        return user.getEducationCode() != null
                && policy.getEducationCode() != null
                && user.getEducationCode() == policy.getEducationCode();
    }

    private boolean matchesEmployment(User user, Policy policy) {
        return user.getEmploymentCode() != null
                && policy.getEmploymentCode() != null
                && user.getEmploymentCode() == policy.getEmploymentCode();
    }
}
