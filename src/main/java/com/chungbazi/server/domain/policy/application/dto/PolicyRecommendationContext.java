package com.chungbazi.server.domain.policy.application.dto;

import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import com.chungbazi.server.domain.policy.domain.entity.RecentViewedPolicy;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.user.domain.UserInterest;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public record PolicyRecommendationContext(
        Set<PolicySubCategoryType> interestSubCategories,
        Map<PolicyCategoryType, Long> interestCategoryCounts,
        Map<PolicySubCategoryType, Double> likedSubCategoryAffinities,
        Map<PolicySubCategoryType, Double> recentViewedSubCategoryAffinities,
        Set<Long> recentViewedPolicyIds
) {
    public static PolicyRecommendationContext of(
            List<UserInterest> interests,
            List<PolicyLike> likes,
            List<RecentViewedPolicy> recentViews
    ) {
        Set<PolicySubCategoryType> interestSubCategories = toInterestSubCategories(interests);

        return PolicyRecommendationContext.builder()
                .interestSubCategories(interestSubCategories)
                .interestCategoryCounts(toInterestCategoryCounts(interestSubCategories))
                .likedSubCategoryAffinities(toSubCategoryAffinities(
                        likes.stream().map(like -> like.getPolicy().getSubCategory()).toList()
                ))
                .recentViewedSubCategoryAffinities(toSubCategoryAffinities(
                        recentViews.stream().map(view -> view.getPolicy().getSubCategory()).toList()
                ))
                .recentViewedPolicyIds(toRecentViewedPolicyIds(recentViews))
                .build();
    }

    public boolean hasInterestSubCategory(PolicySubCategoryType subCategory) {
        return interestSubCategories.contains(subCategory);
    }

    public int interestCategoryCount(PolicyCategoryType category) {
        return interestCategoryCounts.getOrDefault(category, 0L).intValue();
    }

    public int likedSubCategoryScore(
            PolicySubCategoryType subCategory,
            int maxScore,
            double scorePerAffinity
    ) {
        return affinityScore(
                likedSubCategoryAffinities,
                subCategory,
                maxScore,
                scorePerAffinity
        );
    }

    public int recentViewedSubCategoryScore(
            PolicySubCategoryType subCategory,
            int maxScore,
            double scorePerAffinity
    ) {
        return affinityScore(
                recentViewedSubCategoryAffinities,
                subCategory,
                maxScore,
                scorePerAffinity
        );
    }

    public boolean hasRecentlyViewedPolicy(Long policyId) {
        return recentViewedPolicyIds.contains(policyId);
    }

    private static Set<PolicySubCategoryType> toInterestSubCategories(
            List<UserInterest> interests
    ) {
        return interests.stream()
                .map(UserInterest::getSubCategory)
                .collect(Collectors.toSet());
    }

    private static Map<PolicyCategoryType, Long> toInterestCategoryCounts(
            Set<PolicySubCategoryType> interestSubCategories
    ) {
        return interestSubCategories.stream()
                .collect(Collectors.groupingBy(
                        PolicySubCategoryType::getCategory,
                        Collectors.counting()
                ));
    }

    private static Map<PolicySubCategoryType, Double> toSubCategoryAffinities(
            List<PolicySubCategoryType> subCategories
    ) {
        Map<PolicySubCategoryType, Double> affinities = new java.util.EnumMap<>(
                PolicySubCategoryType.class
        );
        for (int rank = 0; rank < subCategories.size(); rank++) {
            PolicySubCategoryType subCategory = subCategories.get(rank);
            double recencyWeight = Math.exp(-rank / 10.0);
            affinities.merge(subCategory, recencyWeight, Double::sum);
        }
        return Map.copyOf(affinities);
    }

    private int affinityScore(
            Map<PolicySubCategoryType, Double> affinities,
            PolicySubCategoryType subCategory,
            int maxScore,
            double scorePerAffinity
    ) {
        if (affinities == null || affinities.isEmpty() || subCategory == null || maxScore <= 0 || scorePerAffinity <= 0) {
            return 0;
        }
        double affinity = affinities.getOrDefault(subCategory, 0.0);
        if (affinity <= 0) {
            return 0;
        }
        return (int) Math.round(Math.min(maxScore, affinity * scorePerAffinity));
    }

    private static Set<Long> toRecentViewedPolicyIds(List<RecentViewedPolicy> recentViews) {
        return recentViews.stream()
                .map(recentView -> recentView.getPolicy().getId())
                .collect(Collectors.toSet());
    }
}
