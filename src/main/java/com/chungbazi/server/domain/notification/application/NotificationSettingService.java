package com.chungbazi.server.domain.notification.application;

import com.chungbazi.server.domain.notification.api.dto.request.NotificationSettingUpdateRequest;
import com.chungbazi.server.domain.notification.api.dto.response.NotificationSettingResponse;
import com.chungbazi.server.domain.notification.application.validator.NotificationSettingValidator;
import com.chungbazi.server.domain.notification.domain.NotificationSetting;
import com.chungbazi.server.domain.notification.domain.repository.NotificationSettingRepository;
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

    @Transactional
    public NotificationSettingResponse getNotificationSetting(User user) {
        return NotificationSettingResponse.from(getOrCreateSetting(user));
    }

    @Transactional
    public NotificationSettingResponse updateNotificationSetting(
            User user,
            NotificationSettingUpdateRequest request
    ) {
        notificationSettingValidator.validate(request);

        NotificationSetting setting = getOrCreateSetting(user);
        setting.updateNotificationSetting(
                request.allNotificationEnabled(),
                request.policyNotificationEnabled(),
                request.chungbaziNotificationEnabled()
        );

        return NotificationSettingResponse.from(setting);
    }

    private NotificationSetting getOrCreateSetting(User user) {
        return notificationSettingRepository.findByUser(user)
                .orElseGet(() -> notificationSettingRepository.save(NotificationSetting.create(user)));
    }
}
