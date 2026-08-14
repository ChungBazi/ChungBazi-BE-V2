package com.chungbazi.server.domain.notification.application;

import com.chungbazi.server.domain.notification.api.dto.request.NotificationSettingUpdateRequest;
import com.chungbazi.server.domain.notification.api.dto.response.NotificationSettingResponse;
import com.chungbazi.server.domain.notification.application.validator.NotificationSettingValidator;
import com.chungbazi.server.domain.notification.domain.NotificationSetting;
import com.chungbazi.server.domain.notification.domain.repository.NotificationSettingRepository;
import com.chungbazi.server.domain.notification.exception.NotificationErrorCode;
import com.chungbazi.server.domain.notification.exception.NotificationException;
import com.chungbazi.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final NotificationSettingValidator notificationSettingValidator;

    public NotificationSettingResponse getNotificationSetting(User user) {
        return NotificationSettingResponse.from(getSetting(user));
    }

    @Transactional
    public NotificationSettingResponse updateNotificationSetting(
            User user,
            NotificationSettingUpdateRequest request
    ) {
        notificationSettingValidator.validate(request);

        NotificationSetting setting = getSetting(user);
        setting.updateNotificationSetting(
                request.allNotificationEnabled(),
                request.policyNotificationEnabled(),
                request.chungbaziNotificationEnabled()
        );

        return NotificationSettingResponse.from(setting);
    }

    private NotificationSetting getSetting(User user) {
        return notificationSettingRepository.findByUser(user)
                .orElseThrow(() -> new NotificationException(
                        NotificationErrorCode.NOTIFICATION_SETTING_NOT_FOUND
                ));
    }
}
