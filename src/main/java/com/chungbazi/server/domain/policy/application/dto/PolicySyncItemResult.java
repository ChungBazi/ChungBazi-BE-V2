package com.chungbazi.server.domain.policy.application.dto;

public record PolicySyncItemResult(
        PolicySyncStatus status,
        Long policyId
) {
    public static PolicySyncItemResult of(PolicySyncStatus status, Long policyId) {
        return new PolicySyncItemResult(status, policyId);
    }
}
