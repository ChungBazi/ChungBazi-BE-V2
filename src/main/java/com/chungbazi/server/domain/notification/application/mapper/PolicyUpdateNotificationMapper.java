package com.chungbazi.server.domain.notification.application.mapper;

import com.chungbazi.server.domain.notification.application.dto.NotificationPushMessage;
import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
import com.chungbazi.server.domain.notification.domain.type.NotificationType;
import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PolicyUpdateNotificationMapper {

    private static final String TITLE = "찜한 정책의 정보가 변경됐어요!";
    private static final String MESSAGE_BODY =
            "신청에 영향을 줄 수 있는 내용이 달라졌어요. 변경된 내용을 확인해보세요.";

    public Notification toNotification(
            PolicyLike policyLike,
            LocalDateTime sourceModifiedAt
    ) {
        return Notification.create(
                policyLike.getUserId(),
                NotificationCategory.MY_POLICY,
                NotificationType.POLICY_UPDATED,
                TITLE,
                "찜한 정책 '%s': %s".formatted(
                        policyLike.getPolicy().getTitle(),
                        MESSAGE_BODY
                ),
                policyLike.getPolicy().getId(),
                sourceModifiedAt
        );
    }

    public List<NotificationPushMessage> toPushMessages(List<Notification> notifications) {
        return notifications.stream()
                .map(NotificationPushMessage::from)
                .toList();
    }
}
