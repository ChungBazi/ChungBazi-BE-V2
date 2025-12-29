package chungbazi.chungbazi_be.domain.notification.dto;

import chungbazi.chungbazi_be.domain.notification.entity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


public class NotificationResponseDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class responseDto{
        private Long notificationId;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class notificationsDto {
        private Long notificationId;
        private boolean isRead;
        private String message;
        private NotificationType type;
        private Long targetId;
        private LocalDateTime formattedCreatedAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class notificationDto {
        private Long notificationId;
        private boolean isRead;
        private String message;
        private NotificationType type;
        private Long targetId;
        private String formattedCreatedAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class notificationListDto{
        private List<notificationsDto> notifications;
        private Long nextCursor;
        private boolean hasNext;
    }

}
