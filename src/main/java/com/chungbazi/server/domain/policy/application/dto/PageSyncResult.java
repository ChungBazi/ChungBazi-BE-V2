package com.chungbazi.server.domain.policy.application.dto;

public record PageSyncResult (
            int fetchedCount,
            int insertedCount,
            int updatedCount,
            int unchangedCount,
            int skippedCount,
            int closedSkippedCount,
            int invalidRegionCount
){}
