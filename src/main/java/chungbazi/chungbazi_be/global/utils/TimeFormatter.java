package chungbazi.chungbazi_be.global.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class TimeFormatter {
    public static String formatCreatedAt(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, now);

        Long minutes = duration.toMinutes();
        Long hours = duration.toHours();
        Long days = duration.toDays();

        if(minutes < 60){
            return minutes + "분전";
        } else if (hours < 24) {
            return createdAt.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else {
            return createdAt.format(DateTimeFormatter.ofPattern("MM/dd"));
        }

    }


    public static YearMonth parseYearMonth(String raw) {
        try {
            String value = raw.replace(".", "").trim(); // "2025.12." -> "202512"
            if (value.length() >= 6) {
                int year = Integer.parseInt(value.substring(0, 4));
                int month = Integer.parseInt(value.substring(4, 6));
                return YearMonth.of(year, month);
            }
        } catch (Exception ignored) {}
        return null;
    }

}
