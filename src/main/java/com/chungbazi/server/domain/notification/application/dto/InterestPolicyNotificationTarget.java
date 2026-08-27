package com.chungbazi.server.domain.notification.application.dto;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.user.domain.User;
import java.util.List;

public record InterestPolicyNotificationTarget(
        User user,
        List<Policy> policies
) {
    public static InterestPolicyNotificationTarget of(
            User user,
            List<Policy> policies
    ) {
        return new InterestPolicyNotificationTarget(user, List.copyOf(policies));
    }
}
