package com.chungbazi.server.domain.policy.application.dto;

import java.util.List;

public record PageSyncResult (
            int fetchedCount,
            int insertedCount,
            int updatedCount,
            int unchangedCount,
            int skippedCount,
            int invalidRegionCount,
            int invalidCategoryCount,
            List<Long> insertedPolicyIds
) {
    public PageSyncResult {
        insertedPolicyIds = List.copyOf(insertedPolicyIds);
    }
}
