package com.chungbazi.server.domain.notification.application.dto;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import java.util.List;

public record PolicyReminderTargets(
        List<Policy> deadlineInSevenDaysPolicies,
        List<Policy> deadlineInThreeDaysPolicies
) {
    public static PolicyReminderTargets of(
            List<Policy> deadlineInSevenDaysPolicies,
            List<Policy> deadlineInThreeDaysPolicies
    ) {
        return new PolicyReminderTargets(deadlineInSevenDaysPolicies, deadlineInThreeDaysPolicies);
    }
}
