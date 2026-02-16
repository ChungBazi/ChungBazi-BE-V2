package chungbazi.chungbazi_be.domain.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class NotificationSettingResponseDTO {
    @Getter
    @Builder
    @AllArgsConstructor
    public static class settingResDto {
        boolean policyAlarm;
        boolean communityAlarm;
        boolean rewardAlarm;
        boolean noticeAlarm;
    }
}
