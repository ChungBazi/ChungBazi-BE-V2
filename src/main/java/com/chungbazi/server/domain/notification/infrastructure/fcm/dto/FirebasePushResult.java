package com.chungbazi.server.domain.notification.infrastructure.fcm.dto;

import java.util.Set;

public record FirebasePushResult(
        int successCount,
        int failureCount,
        Set<String> invalidFcmTokens
) {
    public static FirebasePushResult of(
            int successCount,
            int failureCount,
            Set<String> invalidFcmTokens
    ) {
        return new FirebasePushResult(successCount, failureCount, Set.copyOf(invalidFcmTokens));
    }
}
