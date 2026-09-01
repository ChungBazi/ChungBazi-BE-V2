package com.chungbazi.server.domain.policy.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chungbazi.server.domain.policy.application.dto.PolicyRecommendationContext;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import com.chungbazi.server.domain.policy.domain.entity.RecentViewedPolicy;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PolicyRecommendationContextTest {

    @Test
    @DisplayName("같은 소분류를 반복해서 찜하면 단일 찜보다 높은 점수를 부여한다")
    void givesHigherScoreToRepeatedLikes() {
        PolicyRecommendationContext context = PolicyRecommendationContext.of(
                List.of(),
                List.of(
                        like(PolicySubCategoryType.EMPLOYMENT_PREPARATION),
                        like(PolicySubCategoryType.EMPLOYMENT_PREPARATION),
                        like(PolicySubCategoryType.HOUSING_COST_SPACE)
                ),
                List.of()
        );

        int repeatedScore = context.likedSubCategoryScore(
                PolicySubCategoryType.EMPLOYMENT_PREPARATION,
                30,
                10.0
        );
        int singleScore = context.likedSubCategoryScore(
                PolicySubCategoryType.HOUSING_COST_SPACE,
                30,
                10.0
        );
        assertThat(repeatedScore).isGreaterThan(singleScore);
    }

    @Test
    @DisplayName("같은 소분류를 반복해서 조회하면 단일 조회보다 높은 점수를 부여한다")
    void givesHigherScoreToRepeatedViews() {
        PolicyRecommendationContext context = PolicyRecommendationContext.of(
                List.of(),
                List.of(),
                List.of(
                        view(PolicySubCategoryType.HOUSING_COST_SPACE),
                        view(PolicySubCategoryType.HOUSING_COST_SPACE),
                        view(PolicySubCategoryType.EMPLOYMENT_PREPARATION)
                )
        );

        int repeatedScore = context.recentViewedSubCategoryScore(
                PolicySubCategoryType.HOUSING_COST_SPACE,
                10,
                3.0
        );
        int singleScore = context.recentViewedSubCategoryScore(
                PolicySubCategoryType.EMPLOYMENT_PREPARATION,
                10,
                3.0
        );

        assertThat(repeatedScore).isGreaterThan(singleScore);
    }

    private PolicyLike like(PolicySubCategoryType subCategory) {
        Policy policy = policy(subCategory);
        PolicyLike like = mock(PolicyLike.class);
        when(like.getPolicy()).thenReturn(policy);
        return like;
    }

    private RecentViewedPolicy view(PolicySubCategoryType subCategory) {
        Policy policy = policy(subCategory);
        RecentViewedPolicy view = mock(RecentViewedPolicy.class);
        when(view.getPolicy()).thenReturn(policy);
        return view;
    }

    private Policy policy(PolicySubCategoryType subCategory) {
        Policy policy = mock(Policy.class);
        when(policy.getSubCategory()).thenReturn(subCategory);
        return policy;
    }
}
