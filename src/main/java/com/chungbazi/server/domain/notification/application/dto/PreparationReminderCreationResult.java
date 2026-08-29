package com.chungbazi.server.domain.notification.application.dto;

public record PreparationReminderCreationResult(
        int targetCount,
        int createdNotificationCount
) {
    public static PreparationReminderCreationResult of(
            int targetCount,
            int createdNotificationCount
    ) {
        return new PreparationReminderCreationResult(targetCount, createdNotificationCount);
    }
}
