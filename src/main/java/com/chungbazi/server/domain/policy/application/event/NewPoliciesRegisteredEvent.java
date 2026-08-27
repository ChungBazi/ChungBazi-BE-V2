package com.chungbazi.server.domain.policy.application.event;

import java.util.List;

public record NewPoliciesRegisteredEvent(
        List<Long> policyIds
) {
    public static NewPoliciesRegisteredEvent of(List<Long> policyIds) {
        return new NewPoliciesRegisteredEvent(List.copyOf(policyIds));
    }
}
