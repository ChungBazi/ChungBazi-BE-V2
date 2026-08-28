package com.chungbazi.server.domain.notification.application.event;


import com.chungbazi.server.domain.notification.application.dto.NotificationPushMessage;

import java.util.List;

public record InterestPolicyNotificationsCreatedEvent(
        List<NotificationPushMessage> messages
) {

    public static InterestPolicyNotificationsCreatedEvent of(
            List<NotificationPushMessage> message
    ) {
        return new InterestPolicyNotificationsCreatedEvent(List.copyOf(message));
    }

}
