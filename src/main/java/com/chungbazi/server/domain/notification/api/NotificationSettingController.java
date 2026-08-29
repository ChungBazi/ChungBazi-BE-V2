package com.chungbazi.server.domain.notification.api;

import com.chungbazi.server.domain.notification.api.docs.NotificationSettingDocs;
import com.chungbazi.server.domain.notification.api.dto.request.NotificationSettingUpdateRequest;
import com.chungbazi.server.domain.notification.api.dto.response.NotificationSettingResponse;
import com.chungbazi.server.domain.notification.application.NotificationSettingService;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notifications/settings")
public class NotificationSettingController implements NotificationSettingDocs {

    private final NotificationSettingService notificationSettingService;

    @Override
    @GetMapping
    public CommonResponse<NotificationSettingResponse> getNotificationSetting(
            @CurrentUser User user
    ) {
        return CommonResponse.onSuccess(
                notificationSettingService.getNotificationSetting(user)
        );
    }

    @Override
    @PutMapping
    public CommonResponse<NotificationSettingResponse> updateNotificationSetting(
            @CurrentUser User user,
            @Valid @RequestBody NotificationSettingUpdateRequest request
    ) {
        return CommonResponse.onSuccess(
                notificationSettingService.updateNotificationSetting(user, request)
        );
    }
}
