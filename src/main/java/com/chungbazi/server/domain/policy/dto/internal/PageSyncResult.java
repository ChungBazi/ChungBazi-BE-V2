package com.chungbazi.server.domain.policy.dto.internal;

public record PageSyncResult (
            int fetchedCount,
            int savedCount,
            int skippedCount
){}
