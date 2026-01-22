package chungbazi.chungbazi_be.domain.notification.service;

import chungbazi.chungbazi_be.domain.notification.converter.NotificationConverter;
import chungbazi.chungbazi_be.domain.notification.dto.request.NotificationSettingRequestDTO;
import chungbazi.chungbazi_be.domain.notification.dto.response.NotificationSettingResponseDTO;
import chungbazi.chungbazi_be.domain.notification.entity.NotificationSetting;
import chungbazi.chungbazi_be.domain.notification.repository.NotificationSettingRepository;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {
    private final UserHelper userHelper;
    private final NotificationSettingRepository notificationSettingRepository;

    //알림 수신 설정
    @Transactional
    public NotificationSettingResponseDTO.settingResDto setNotificationSetting(NotificationSettingRequestDTO dto){
        User user=userHelper.getAuthenticatedUser();

        NotificationSetting setting= user.getNotificationSetting();

        if (setting == null) {
            NotificationSetting notificationSetting = NotificationSetting.builder()
                    .user(user)
                    .build();

            setting = notificationSettingRepository.save(notificationSetting);
        }

        setting.updateNotificationSetting(dto.isPolicyAlarm(), dto.isCommunityAlarm(), dto.isRewardAlarm(), dto.isNoticeAlarm());

        user.updateNotificationSetting(setting);
        notificationSettingRepository.save(setting);

        return NotificationConverter.toSettingResDto(setting);
    }

    //알림 수신 설정 조회
    @Transactional
    public NotificationSettingResponseDTO.settingResDto getNotificationSetting(){
        User user=userHelper.getAuthenticatedUser();
        NotificationSetting setting=user.getNotificationSetting();

        if (setting == null) {
            NotificationSetting notificationSetting = NotificationSetting.builder()
                    .user(user)
                    .build();

            setting = notificationSettingRepository.save(notificationSetting);
        }

        return NotificationConverter.toSettingResDto(setting);
    }
}
