package com.chungbazi.server.domain.policy.application.dto;

public record SyncResult(
        int fetchedCount,
        int savedCount,
        int skippedCount
) {
}
