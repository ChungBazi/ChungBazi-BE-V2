package com.chungbazi.server.domain.notification.api;

import com.chungbazi.server.domain.notification.api.docs.NotificationDocs;
import com.chungbazi.server.domain.notification.api.dto.response.NotificationListResponse;
import com.chungbazi.server.domain.notification.application.NotificationService;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notifications")
public class NotificationController implements NotificationDocs {

    private final NotificationService notificationService;

    @Override
    @GetMapping
    public CommonResponse<NotificationListResponse> getNotifications(
            @CurrentUser User user,
            @RequestParam(required = false) NotificationCategory category,
            @RequestParam(required = false) @Min(1) Long cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return CommonResponse.onSuccess(
                notificationService.getNotifications(user, category, cursor, size)
        );
    }
}
