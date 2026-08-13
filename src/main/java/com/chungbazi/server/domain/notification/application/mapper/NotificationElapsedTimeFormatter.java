package com.chungbazi.server.domain.notification.application.mapper;

import java.time.Duration;
import java.time.LocalDateTime;

public final class NotificationElapsedTimeFormatter {

    private static final long MINUTES_PER_HOUR = 60;
    private static final long HOURS_PER_DAY = 24;

    private NotificationElapsedTimeFormatter() {
    }

    public static String format(LocalDateTime createdAt, LocalDateTime now) {
        if (createdAt == null || !createdAt.isBefore(now)) {
            return "방금";
        }

        long elapsedMinutes = Duration.between(createdAt, now).toMinutes();
        if (elapsedMinutes < 1) {
            return "방금";
        }
        if (elapsedMinutes < MINUTES_PER_HOUR) {
            return elapsedMinutes + "분 전";
        }

        long elapsedHours = elapsedMinutes / MINUTES_PER_HOUR;
        if (elapsedHours < HOURS_PER_DAY) {
            return elapsedHours + "시간 전";
        }

        return elapsedHours / HOURS_PER_DAY + "일 전";
    }
}
