package com.chungbazi.server.domain.notification.api.dto.response;

import com.chungbazi.server.domain.notification.domain.NotificationSetting;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "알림 설정 응답")
public record NotificationSettingResponse(
        @Schema(description = "전체 알림 수신 여부", example = "true")
        boolean allNotificationEnabled,

        @Schema(description = "내 정책 알림 수신 여부", example = "true")
        boolean policyNotificationEnabled,

        @Schema(description = "청바지 알림 수신 여부", example = "true")
        boolean chungbaziNotificationEnabled
) {
    public static NotificationSettingResponse from(NotificationSetting setting) {
        return NotificationSettingResponse.builder()
                .allNotificationEnabled(setting.isAllNotificationEnabled())
                .policyNotificationEnabled(setting.isPolicyNotificationEnabled())
                .chungbaziNotificationEnabled(setting.isChungbaziNotificationEnabled())
                .build();
    }
}
