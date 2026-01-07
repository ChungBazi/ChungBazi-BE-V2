package chungbazi.chungbazi_be.domain.notification.converter;

import chungbazi.chungbazi_be.domain.notification.dto.internal.NotificationData;
import chungbazi.chungbazi_be.domain.notification.dto.response.NotificationSettingResponseDTO;
import chungbazi.chungbazi_be.domain.notification.entity.NotificationSetting;
import chungbazi.chungbazi_be.domain.notification.entity.enums.NotificationType;
import chungbazi.chungbazi_be.domain.user.entity.User;

public class NotificationConverter {

    public static NotificationSettingResponseDTO.settingResDto toSettingResDto(NotificationSetting notificationSetting){
        return NotificationSettingResponseDTO.settingResDto.builder()
                .policyAlarm(notificationSetting.isPolicyAlarm())
                .communityAlarm(notificationSetting.isCommunityAlarm())
                .rewardAlarm(notificationSetting.isRewardAlarm())
                .noticeAlarm(notificationSetting.isNoticeAlarm())
                .build();
    }

    public static NotificationData toCommunityEntity(User receiver, String message, Long targetId) {
        return NotificationData.builder()
                .user(receiver)
                .type(NotificationType.COMMUNITY)
                .message(message)
                .targetId(targetId)
                .build();
    }
}
