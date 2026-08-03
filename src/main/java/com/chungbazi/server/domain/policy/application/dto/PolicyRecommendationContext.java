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
        Set<PolicySubCategoryType> likedSubCategories,
        Set<PolicySubCategoryType> recentViewedSubCategories,
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
                .likedSubCategories(toLikedSubCategories(likes))
                .recentViewedSubCategories(toRecentViewedSubCategories(recentViews))
                .recentViewedPolicyIds(toRecentViewedPolicyIds(recentViews))
                .build();
    }

    public boolean hasInterestSubCategory(PolicySubCategoryType subCategory) {
        return interestSubCategories.contains(subCategory);
    }

    public int interestCategoryCount(PolicyCategoryType category) {
        return interestCategoryCounts.getOrDefault(category, 0L).intValue();
    }

    public boolean hasLikedSubCategory(PolicySubCategoryType subCategory) {
        return likedSubCategories.contains(subCategory);
    }

    public boolean hasRecentlyViewedSubCategory(PolicySubCategoryType subCategory) {
        return recentViewedSubCategories.contains(subCategory);
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

    private static Set<PolicySubCategoryType> toLikedSubCategories(
            List<PolicyLike> likes
    ) {
        return likes.stream()
                .map(like -> like.getPolicy().getSubCategory())
                .collect(Collectors.toSet());
    }

    private static Set<PolicySubCategoryType> toRecentViewedSubCategories(
            List<RecentViewedPolicy> recentViews
    ) {
        return recentViews.stream()
                .map(recentView -> recentView.getPolicy().getSubCategory())
                .collect(Collectors.toSet());
    }

    private static Set<Long> toRecentViewedPolicyIds(
            List<RecentViewedPolicy> recentViews
    ) {
        return recentViews.stream()
                .map(recentView -> recentView.getPolicy().getId())
                .collect(Collectors.toSet());
    }
}
