package com.chungbazi.server.domain.notification.application.dto;

import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.type.NotificationType;

public record NotificationKey(
        Long userId,
        Long policyId,
        NotificationType type
) {
    public static NotificationKey from(Notification notification) {
        return new NotificationKey(
                notification.getUserId(),
                notification.getPolicyId(),
                notification.getType()
        );
    }
}
