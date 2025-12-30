package chungbazi.chungbazi_be.domain.notification.service;

import chungbazi.chungbazi_be.domain.notification.converter.NotificationConverter;
import chungbazi.chungbazi_be.domain.notification.dto.NotificationSettingReqDto;
import chungbazi.chungbazi_be.domain.notification.dto.NotificationSettingResDto;
import chungbazi.chungbazi_be.domain.notification.entity.NotificationSetting;
import chungbazi.chungbazi_be.domain.notification.repository.NotificationSettingRepository;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {
    private final UserHelper userHelper;
    private final NotificationSettingRepository notificationSettingRepository;

    //알림 수신 설정
    public NotificationSettingResDto.settingResDto setNotificationSetting(NotificationSettingReqDto dto){
        User user=userHelper.getAuthenticatedUser();

        NotificationSetting setting=user.getNotificationSetting();

        setting.updatePolicyAlarm(dto.isPolicyAlarm());
        setting.updateCommunityAlarm(dto.isCommunityAlarm());
        setting.updateRewardAlarm(dto.isRewardAlarm());
        setting.updateNoticeAlarm(dto.isNoticeAlarm());

        user.updateNotificationSetting(setting);
        notificationSettingRepository.save(setting);

        return NotificationConverter.toSettingResDto(setting);
    }

    //알림 수신 설정 조회
    public NotificationSettingResDto.settingResDto getNotificationSetting(){
        User user=userHelper.getAuthenticatedUser();
        NotificationSetting setting=user.getNotificationSetting();

        return NotificationConverter.toSettingResDto(setting);
    }
}
