package com.chungbazi.server.domain.policy.application.event;

import java.util.List;

public record PolicySearchIndexRefreshEvent(
        List<Long> changedPolicyIds
) {
    public PolicySearchIndexRefreshEvent {
        changedPolicyIds = List.copyOf(changedPolicyIds);
    }
    public static PolicySearchIndexRefreshEvent of(List<Long> changedPolicyIds) {
        return new PolicySearchIndexRefreshEvent(changedPolicyIds);
    }
}
