package com.chungbazi.server.domain.notification.application.dto;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.user.domain.User;
import java.util.List;

public record PersonalizedPolicyNotificationTarget(
        User user,
        List<Policy> policies
) {
    public static PersonalizedPolicyNotificationTarget of(
            User user,
            List<Policy> policies
    ) {
        return new PersonalizedPolicyNotificationTarget(user, List.copyOf(policies));
    }
}
