package com.chungbazi.server.domain.notification.application.mapper;

import com.chungbazi.server.domain.notification.application.dto.NotificationPushMessage;
import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
import com.chungbazi.server.domain.notification.domain.type.NotificationType;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.user.domain.User;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class InterestPolicyNotificationMapper {

    private static final String TITLE_FORMAT = "%s에 정책이 등록됐어요";
    private static final String MESSAGE =
            "관심 있게 보고 있는 분야에서 받을 수 있는 새로운 지원을 확인해보세요.";

    public Notification toNotification(User user, Policy policy) {
        return Notification.create(
                user.getId(),
                NotificationCategory.CHUNGBAZI,
                NotificationType.INTEREST_POLICY,
                TITLE_FORMAT.formatted(policy.getSubCategory().getDescription()),
                MESSAGE,
                policy.getId()
        );
    }

    public List<NotificationPushMessage> toRepresentativePushMessages(
            List<Notification> notifications
    ) {
        Map<Long, NotificationPushMessage> messageByUserId = new LinkedHashMap<>();
        notifications.forEach(notification -> messageByUserId.putIfAbsent(
                notification.getUserId(),
                NotificationPushMessage.from(notification)
        ));
        return List.copyOf(messageByUserId.values());
    }

}
