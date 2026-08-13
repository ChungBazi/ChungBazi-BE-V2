package com.chungbazi.server.domain.notification.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "알림 설정 변경 요청")
public record NotificationSettingUpdateRequest(
        @NotNull
        @Schema(description = "전체 알림 수신 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean allNotificationEnabled,

        @NotNull
        @Schema(description = "내 정책 알림 수신 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean policyNotificationEnabled,

        @NotNull
        @Schema(description = "청바지 알림 수신 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean chungbaziNotificationEnabled
) {
}
