package com.chungbazi.server.domain.notification.application.dto;

public record DeadlineReminderCreationResult(
        int deadlineInSevenDaysPolicyCount,
        int deadlineInThreeDaysPolicyCount,
        int createdNotificationCount
) {
    public static DeadlineReminderCreationResult of(
            int deadlineInSevenDaysPolicyCount,
            int deadlineInThreeDaysPolicyCount,
            int createdNotificationCount
    ) {
        return new DeadlineReminderCreationResult(
                deadlineInSevenDaysPolicyCount,
                deadlineInThreeDaysPolicyCount,
                createdNotificationCount
        );
    }
}
