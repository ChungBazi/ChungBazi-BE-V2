package com.chungbazi.server.domain.policy.application.dto;

import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import com.chungbazi.server.domain.policy.domain.entity.RecentViewedPolicy;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.user.domain.UserInterest;
import lombok.Builder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public record PolicyRecommendationContext(
        Set<PolicySubCategoryType> interestSubCategories,
        Set<PolicySubCategoryType> likedSubCategories,
        Set<PolicySubCategoryType> recentViewedSubCategories,
        Set<Long> recentViewedPolicyIds
) {
    public static PolicyRecommendationContext of(
            List<UserInterest> interests,
            List<PolicyLike> likes,
            List<RecentViewedPolicy> recentViews
    ) {
        return new PolicyRecommendationContext(
                interests.stream()
                        .map(UserInterest::getSubCategory)
                        .collect(Collectors.toSet()),
                likes.stream()
                        .map(like -> like.getPolicy().getSubCategory())
                        .collect(Collectors.toSet()),
                recentViews.stream()
                        .map(view -> view.getPolicy().getSubCategory())
                        .collect(Collectors.toSet()),
                recentViews.stream()
                        .map(view -> view.getPolicy().getId())
                        .collect(Collectors.toSet())
        );
    }

    public boolean hasInterestSubCategory(PolicySubCategoryType subCategory) {
        return interestSubCategories.contains(subCategory);
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
}
