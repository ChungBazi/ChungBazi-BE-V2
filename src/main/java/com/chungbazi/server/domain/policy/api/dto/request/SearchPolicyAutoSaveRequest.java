package com.chungbazi.server.domain.policy.api.dto.request;

import lombok.Builder;

@Builder
public record SearchPolicyAutoSaveRequest(
        boolean enabled
) {
}
