package chungbazi.chungbazi_be.domain.notification.converter;

import chungbazi.chungbazi_be.domain.notification.dto.NotificationSettingResDto;
import chungbazi.chungbazi_be.domain.notification.entity.NotificationSetting;

public class NotificationConverter {

    public static NotificationSettingResDto.settingResDto toSettingResDto(NotificationSetting notificationSetting){
        return NotificationSettingResDto.settingResDto.builder()
                .policyAlarm(notificationSetting.isPolicyAlarm())
                .communityAlarm(notificationSetting.isCommunityAlarm())
                .rewardAlarm(notificationSetting.isRewardAlarm())
                .noticeAlarm(notificationSetting.isNoticeAlarm())
                .build();
    }
}
