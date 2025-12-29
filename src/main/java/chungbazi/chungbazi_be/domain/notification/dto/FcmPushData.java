package chungbazi.chungbazi_be.domain.notification.dto;

import chungbazi.chungbazi_be.domain.notification.entity.Notification;
import chungbazi.chungbazi_be.domain.notification.entity.enums.NotificationType;


public record FcmPushData(String message, NotificationType type, Long targetId) {
    public static FcmPushData from(Notification notification) {
        return new FcmPushData(
                notification.getMessage(),
                notification.getType(),
                notification.getTargetId()
        );
    }
}

