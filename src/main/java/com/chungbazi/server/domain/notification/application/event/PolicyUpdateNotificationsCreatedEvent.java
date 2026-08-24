package com.chungbazi.server.domain.notification.application.event;

import com.chungbazi.server.domain.notification.application.dto.NotificationPushMessage;
import java.util.List;

public record PolicyUpdateNotificationsCreatedEvent(
        List<NotificationPushMessage> messages
) {
    public static PolicyUpdateNotificationsCreatedEvent of(
            List<NotificationPushMessage> messages
    ) {
        return new PolicyUpdateNotificationsCreatedEvent(List.copyOf(messages));
    }
}
