package com.chungbazi.server.domain.notification.application.event;

import com.chungbazi.server.domain.notification.application.dto.NotificationPushMessage;
import java.util.List;

public record PersonalizedPolicyNotificationsCreatedEvent(
        List<NotificationPushMessage> messages
) {
    public static PersonalizedPolicyNotificationsCreatedEvent of(
            List<NotificationPushMessage> messages
    ) {
        return new PersonalizedPolicyNotificationsCreatedEvent(List.copyOf(messages));
    }
}
