package com.chungbazi.server.domain.policy.application.dto;

public record SyncResult(
        int fetchedCount,
        int insertedCount,
        int updatedCount,
        int unchangedCount,
        int skippedCount,
        long totalElapsedMillis
) {
}
