package com.chungbazi.server.domain.notification.application.event;

import com.chungbazi.server.domain.notification.application.dto.NotificationPushMessage;
import java.util.List;

public record PolicyReminderNotificationsCreatedEvent(
        List<NotificationPushMessage> messages
) {
    public static PolicyReminderNotificationsCreatedEvent of(
            List<NotificationPushMessage> messages
    ) {
        return new PolicyReminderNotificationsCreatedEvent(List.copyOf(messages));
    }
}
