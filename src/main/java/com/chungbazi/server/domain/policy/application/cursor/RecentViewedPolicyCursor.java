package com.chungbazi.server.domain.policy.application.cursor;

import java.time.LocalDateTime;

public record RecentViewedPolicyCursor(
        LocalDateTime viewedAt,
        Long policyId
) {
}
