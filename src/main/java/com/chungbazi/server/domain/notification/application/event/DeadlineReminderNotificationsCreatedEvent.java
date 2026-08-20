package com.chungbazi.server.domain.notification.application.event;

import com.chungbazi.server.domain.notification.application.dto.NotificationPushMessage;
import java.util.List;

public record DeadlineReminderNotificationsCreatedEvent(
        List<NotificationPushMessage> messages
) {
    public static DeadlineReminderNotificationsCreatedEvent of(
            List<NotificationPushMessage> messages
    ) {
        return new DeadlineReminderNotificationsCreatedEvent(List.copyOf(messages));
    }
}
