package com.chungbazi.server.domain.policy.application.dto;

public record PageSyncResult (
            int fetchedCount,
            int savedCount,
            int skippedCount
){}
