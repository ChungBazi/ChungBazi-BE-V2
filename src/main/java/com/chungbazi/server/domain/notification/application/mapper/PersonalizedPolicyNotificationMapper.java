package com.chungbazi.server.domain.notification.application.mapper;

import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
import com.chungbazi.server.domain.notification.domain.type.NotificationType;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class PersonalizedPolicyNotificationMapper {

    private static final String TITLE = "새로운 맞춤 정책이 도착했어요!";
    private static final String MESSAGE_FORMAT =
            "%s님에게 잘 맞는 정책을 새롭게 가져왔어요! 지금 바로 어떤 정책인지 확인해보세요.";

    public Notification toNotification(User user, Policy policy) {
        return Notification.create(
                user.getId(),
                NotificationCategory.CHUNGBAZI,
                NotificationType.PERSONALIZED_POLICY,
                TITLE,
                MESSAGE_FORMAT.formatted(user.getName()),
                policy.getId()
        );
    }
}
