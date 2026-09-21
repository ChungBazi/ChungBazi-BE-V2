package com.chungbazi.server.domain.notification.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "읽지 않은 알림 존재 여부 응답")
public record NotificationUnreadStatusResponse(
        @Schema(description = "읽지 않은 알림 존재 여부", example = "true")
        boolean hasUnreadNotification
) {
    public static NotificationUnreadStatusResponse of(boolean hasUnreadNotification) {
        return new NotificationUnreadStatusResponse(hasUnreadNotification);
    }
}
