package com.chungbazi.server.domain.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.chungbazi.server.domain.notification.application.dto.NotificationPushMessage;
import com.chungbazi.server.domain.notification.application.event.PolicyUpdateNotificationsCreatedEvent;
import com.chungbazi.server.domain.notification.application.mapper.PolicyUpdateNotificationMapper;
import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.repository.NotificationRepository;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
import com.chungbazi.server.domain.notification.domain.type.NotificationType;
import com.chungbazi.server.domain.policy.application.event.PolicyInformationChangedEvent;
import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PolicyUpdateNotificationServiceTest {

    @Mock
    private PolicyLikeRepository policyLikeRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private PolicyUpdateNotificationMapper policyUpdateNotificationMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PolicyUpdateNotificationService policyUpdateNotificationService;

    @Test
    void publishesPushEventAfterSavingNewNotifications() {
        LocalDateTime sourceModifiedAt = LocalDateTime.of(2026, 8, 24, 10, 0);
        PolicyInformationChangedEvent event = PolicyInformationChangedEvent.of(1L, sourceModifiedAt);
        PolicyLike recipient = org.mockito.Mockito.mock(PolicyLike.class);
        Notification notification = Notification.create(
                10L,
                NotificationCategory.MY_POLICY,
                NotificationType.POLICY_UPDATED,
                "찜한 정책의 정보가 변경됐어요!",
                "정책 정보가 변경됐어요.",
                1L,
                sourceModifiedAt
        );
        NotificationPushMessage pushMessage = NotificationPushMessage.from(notification);

        given(recipient.getUserId()).willReturn(10L);
        given(policyLikeRepository.findNotificationRecipientsByPolicyIds(Set.of(1L)))
                .willReturn(List.of(recipient));
        given(notificationRepository
                .findAllByUserIdInAndPolicyIdAndTypeAndPolicySourceModifiedAt(
                        Set.of(10L),
                        1L,
                        NotificationType.POLICY_UPDATED,
                        sourceModifiedAt
                ))
                .willReturn(List.of());
        given(policyUpdateNotificationMapper.toNotification(recipient, sourceModifiedAt))
                .willReturn(notification);
        given(policyUpdateNotificationMapper.toPushMessages(List.of(notification)))
                .willReturn(List.of(pushMessage));

        policyUpdateNotificationService.createPolicyUpdateNotifications(event);

        verify(notificationRepository).saveAll(List.of(notification));
        ArgumentCaptor<PolicyUpdateNotificationsCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(PolicyUpdateNotificationsCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().messages()).containsExactly(pushMessage);
    }
}
