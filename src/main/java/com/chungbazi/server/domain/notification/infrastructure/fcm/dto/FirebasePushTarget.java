package com.chungbazi.server.domain.notification.infrastructure.fcm.dto;

import com.chungbazi.server.domain.notification.application.dto.NotificationPushMessage;

public record FirebasePushTarget(
        String fcmToken,
        NotificationPushMessage message
) {
    public static FirebasePushTarget of(
            String fcmToken,
            NotificationPushMessage message
    ) {
        return new FirebasePushTarget(fcmToken, message);
    }
}
