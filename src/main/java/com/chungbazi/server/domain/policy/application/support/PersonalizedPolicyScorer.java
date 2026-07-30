package com.chungbazi.server.domain.policy.application.support;

import com.chungbazi.server.domain.policy.application.dto.PolicyRecommendationContext;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.user.domain.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
public class PersonalizedPolicyScorer {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private static final int INTEREST_SUB_CATEGORY_SCORE = 35;
    private static final int INTEREST_CATEGORY_COUNT_SCORE = 10;
    private static final int LIKED_SUB_CATEGORY_SCORE = 30;
    private static final int EDUCATION_SCORE = 8;
    private static final int EMPLOYMENT_SCORE = 10;
    private static final int RECENT_VIEWED_SUB_CATEGORY_SCORE = 10;
    // 최근 본 정책 조회 섹션과 겹치지 않게 페널티 부여
    private static final int RECENT_VIEWED_POLICY_PENALTY = -15;

    private final List<RecommendationRule> rules = List.of(
            RecommendationRule.of(
                    "INTEREST",
                    this::interestScore
            ),
            RecommendationRule.fixed(
                    "LIKED_SUB_CATEGORY",
                    LIKED_SUB_CATEGORY_SCORE,
                    input -> input.context().hasLikedSubCategory(input.policy().getSubCategory())
            ),
            RecommendationRule.fixed(
                    "EDUCATION_MATCH",
                    EDUCATION_SCORE,
                    input -> matchesEducation(input.user(), input.policy())
            ),
            RecommendationRule.fixed(
                    "EMPLOYMENT_MATCH",
                    EMPLOYMENT_SCORE,
                    input -> matchesEmployment(input.user(), input.policy())
            ),
            // TODO: 정책 소득 조건 정규화 기준이 확정되면 소득 매칭 점수 규칙 추가
            RecommendationRule.fixed(
                    "RECENT_VIEWED_SUB_CATEGORY",
                    RECENT_VIEWED_SUB_CATEGORY_SCORE,
                    input -> input.context().hasRecentlyViewedSubCategory(input.policy().getSubCategory())
            ),
            RecommendationRule.fixed(
                    "ALREADY_VIEWED_POLICY",
                    RECENT_VIEWED_POLICY_PENALTY,
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

    private int interestScore(RecommendationInput input) {
        Policy policy = input.policy();
        PolicyRecommendationContext context = input.context();

        int score = 0;

        if (context.hasInterestSubCategory(policy.getSubCategory())) {
            score += INTEREST_SUB_CATEGORY_SCORE;
        }

        score += context.interestCategoryCount(policy.getCategory()) * INTEREST_CATEGORY_COUNT_SCORE;

        return score;
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
