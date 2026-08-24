package com.chungbazi.server.domain.policy.application.event;

import java.time.LocalDateTime;

public record PolicyInformationChangedEvent(
        Long policyId,
        LocalDateTime sourceModifiedAt
) {
    public static PolicyInformationChangedEvent of(
            Long policyId,
            LocalDateTime sourceModifiedAt
    ) {
        return new PolicyInformationChangedEvent(policyId, sourceModifiedAt);
    }
}
