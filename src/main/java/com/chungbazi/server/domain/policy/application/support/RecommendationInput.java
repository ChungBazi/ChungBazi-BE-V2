package com.chungbazi.server.domain.policy.application.support;

import com.chungbazi.server.domain.policy.application.dto.PolicyRecommendationContext;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.user.domain.User;

public record RecommendationInput(
        User user,
        PolicyRecommendationContext context,
        Policy policy
) {
}
