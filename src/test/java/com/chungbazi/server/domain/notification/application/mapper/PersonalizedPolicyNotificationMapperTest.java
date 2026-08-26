package com.chungbazi.server.domain.notification.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.chungbazi.server.domain.notification.application.dto.NotificationPushMessage;
import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
import com.chungbazi.server.domain.notification.domain.type.NotificationType;
import java.util.List;
import org.junit.jupiter.api.Test;

class PersonalizedPolicyNotificationMapperTest {

    private final PersonalizedPolicyNotificationMapper mapper =
            new PersonalizedPolicyNotificationMapper();

    @Test
    void createsOnlyOnePushMessageForMultipleRecommendedPoliciesOfTheSameUser() {
        Notification firstRankedNotification = notification(1L, 10L);
        Notification secondRankedNotification = notification(1L, 20L);
        Notification thirdRankedNotification = notification(1L, 30L);

        List<NotificationPushMessage> messages = mapper.toRepresentativePushMessages(List.of(
                firstRankedNotification,
                secondRankedNotification,
                thirdRankedNotification
        ));

        assertThat(messages).hasSize(1);
        assertThat(messages.getFirst().userId()).isEqualTo(1L);
        assertThat(messages.getFirst().policyId()).isEqualTo(10L);
    }

    private Notification notification(Long userId, Long policyId) {
        return Notification.create(
                userId,
                NotificationCategory.CHUNGBAZI,
                NotificationType.PERSONALIZED_POLICY,
                "새로운 맞춤 정책이 도착했어요!",
                "새로운 맞춤 정책을 확인해보세요.",
                policyId
        );
    }
}
