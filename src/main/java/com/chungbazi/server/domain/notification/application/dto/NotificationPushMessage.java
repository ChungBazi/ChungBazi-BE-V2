package com.chungbazi.server.domain.notification.application.dto;

import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;

public record NotificationPushMessage(
        Long notificationId,
        Long userId,
        NotificationCategory category,
        String title,
        String message,
        Long policyId
) {
    public static NotificationPushMessage from(Notification notification) {
        return new NotificationPushMessage(
                notification.getId(),
                notification.getUserId(),
                notification.getCategory(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getPolicyId()
        );
    }

    public static NotificationPushMessage from(
            Notification notification,
            String pushTitle,
            String pushMessage
    ) {
        return new NotificationPushMessage(
                notification.getId(),
                notification.getUserId(),
                notification.getCategory(),
                pushTitle,
                pushMessage,
                notification.getPolicyId()
        );
    }
}
