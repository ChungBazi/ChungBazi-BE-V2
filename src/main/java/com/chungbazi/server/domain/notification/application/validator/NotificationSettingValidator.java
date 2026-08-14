package com.chungbazi.server.domain.notification.application.validator;

import com.chungbazi.server.domain.notification.api.dto.request.NotificationSettingUpdateRequest;
import com.chungbazi.server.domain.notification.exception.NotificationErrorCode;
import com.chungbazi.server.domain.notification.exception.NotificationException;
import org.springframework.stereotype.Component;

@Component
public class NotificationSettingValidator {

    public void validate(NotificationSettingUpdateRequest request) {
        boolean allEnabled = request.allNotificationEnabled();
        boolean policyEnabled = request.policyNotificationEnabled();
        boolean chungbaziEnabled = request.chungbaziNotificationEnabled();

        if (!allEnabled && (policyEnabled || chungbaziEnabled)) {
            throw new NotificationException(
                    NotificationErrorCode.CHILD_NOTIFICATION_ENABLED_WHILE_ALL_DISABLED
            );
        }

        if (allEnabled && !policyEnabled && !chungbaziEnabled) {
            throw new NotificationException(
                    NotificationErrorCode.ALL_NOTIFICATION_ENABLED_WITHOUT_CHILD
            );
        }
    }
}
