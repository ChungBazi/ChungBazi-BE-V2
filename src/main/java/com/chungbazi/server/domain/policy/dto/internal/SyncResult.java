package com.chungbazi.server.domain.policy.dto.internal;

public record SyncResult(
        int fetchedCount,
        int savedCount,
        int skippedCount
) {
}
