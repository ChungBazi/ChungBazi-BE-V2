package chungbazi.chungbazi_be.domain.notification.converter;

import chungbazi.chungbazi_be.domain.notification.dto.response.NotificationSettingResponseDTO;
import chungbazi.chungbazi_be.domain.notification.entity.NotificationSetting;

public class NotificationConverter {

    public static NotificationSettingResponseDTO.settingResDto toSettingResDto(NotificationSetting notificationSetting){
        return NotificationSettingResponseDTO.settingResDto.builder()
                .policyAlarm(notificationSetting.isPolicyAlarm())
                .communityAlarm(notificationSetting.isCommunityAlarm())
                .rewardAlarm(notificationSetting.isRewardAlarm())
                .noticeAlarm(notificationSetting.isNoticeAlarm())
                .build();
    }
}
