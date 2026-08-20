package com.chungbazi.server.domain.notification.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
import com.chungbazi.server.domain.notification.domain.type.NotificationType;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import org.junit.jupiter.api.Test;

class NotificationReminderMapperTest {

    private final NotificationReminderMapper notificationReminderMapper =
            new NotificationReminderMapper();

    @Test
    void mapsPreparationNotificationWithTheSpecifiedContent() {
        Policy policy = org.mockito.Mockito.mock(Policy.class);
        PolicyLike policyLike = org.mockito.Mockito.mock(PolicyLike.class);
        given(policy.getId()).willReturn(10L);
        given(policyLike.getUserId()).willReturn(1L);
        given(policyLike.getPolicy()).willReturn(policy);

        Notification notification =
                notificationReminderMapper.toPreparationNotification(policyLike);

        assertThat(notification.getUserId()).isEqualTo(1L);
        assertThat(notification.getPolicyId()).isEqualTo(10L);
        assertThat(notification.getCategory()).isEqualTo(NotificationCategory.MY_POLICY);
        assertThat(notification.getType()).isEqualTo(NotificationType.POLICY_PREPARATION);
        assertThat(notification.getTitle()).isEqualTo("찜한 정책, 미리 준비해볼까요?");
        assertThat(notification.getMessage()).isEqualTo(
                "신청할 때 놓치는 내용이 없도록 필요한 정보와 신청 방법을 미리 살펴보세요."
        );
        assertThat(notification.isRead()).isFalse();
    }
}
