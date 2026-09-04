package com.chungbazi.server.domain.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.chungbazi.server.domain.notification.application.dto.NotificationPushMessage;
import com.chungbazi.server.domain.notification.application.dto.PreparationReminderCreationResult;
import com.chungbazi.server.domain.notification.application.event.PolicyReminderNotificationsCreatedEvent;
import com.chungbazi.server.domain.notification.application.mapper.NotificationReminderMapper;
import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.repository.NotificationRepository;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
import com.chungbazi.server.domain.notification.domain.type.NotificationType;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import java.time.LocalDate;
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
class NotificationReminderServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyLikeRepository policyLikeRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationReminderMapper notificationReminderMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private NotificationReminderService notificationReminderService;

    @Test
    void findsLikesInTheHourlyWindowAfterTwentyFourHoursAndExcludesNearDeadlines() {
        LocalDateTime executionTime = LocalDateTime.of(2026, 8, 21, 10, 37);
        LocalDateTime likedFrom = LocalDateTime.of(2026, 8, 20, 9, 0);
        LocalDateTime likedUntil = LocalDateTime.of(2026, 8, 20, 10, 0);

        PolicyLike eligibleLike = policyLike(
                LocalDateTime.of(2026, 8, 20, 9, 30),
                LocalDate.of(2026, 8, 28)
        );
        PolicyLike openEndedLike = policyLike(
                LocalDateTime.of(2026, 8, 20, 9, 40),
                null
        );
        PolicyLike deadlineInSevenDaysLike = policyLike(
                LocalDateTime.of(2026, 8, 20, 9, 50),
                LocalDate.of(2026, 8, 27)
        );

        given(policyLikeRepository.findPreparationReminderTargets(
                likedFrom,
                likedUntil,
                RecruitmentStatus.CLOSED
        )).willReturn(List.of(
                eligibleLike,
                openEndedLike,
                deadlineInSevenDaysLike
        ));

        List<PolicyLike> result =
                notificationReminderService.findPreparationReminderTargets(executionTime);

        assertThat(result).containsExactly(eligibleLike, openEndedLike);
        verify(policyLikeRepository).findPreparationReminderTargets(
                likedFrom,
                likedUntil,
                RecruitmentStatus.CLOSED
        );
    }

    @Test
    void savesOnlyNewPreparationNotificationsAndPublishesTheirPushEvent() {
        LocalDateTime executionTime = LocalDateTime.of(2026, 8, 21, 10, 37);
        LocalDateTime likedFrom = LocalDateTime.of(2026, 8, 20, 9, 0);
        LocalDateTime likedUntil = LocalDateTime.of(2026, 8, 20, 10, 0);
        PolicyLike newTarget = preparationTarget(1L, 10L);
        PolicyLike duplicateTarget = preparationTarget(2L, 20L);

        Notification newNotification = preparationNotification(1L, 10L);
        Notification duplicateNotification = preparationNotification(2L, 20L);
        Notification existingNotification = preparationNotification(2L, 20L);
        List<NotificationPushMessage> pushMessages = List.of(
                NotificationPushMessage.from(newNotification)
        );

        given(policyLikeRepository.findPreparationReminderTargets(
                likedFrom,
                likedUntil,
                RecruitmentStatus.CLOSED
        )).willReturn(List.of(newTarget, duplicateTarget));
        given(notificationRepository.findAllByUserIdInAndPolicyIdInAndTypeIn(
                Set.of(1L, 2L),
                Set.of(10L, 20L),
                Set.of(NotificationType.POLICY_PREPARATION)
        )).willReturn(List.of(existingNotification));
        given(notificationReminderMapper.toPreparationNotification(newTarget))
                .willReturn(newNotification);
        given(notificationReminderMapper.toPreparationNotification(duplicateTarget))
                .willReturn(duplicateNotification);
        given(notificationReminderMapper.toRepresentativePushMessages(
                List.of(newNotification),
                List.of(newTarget, duplicateTarget)
        ))
                .willReturn(pushMessages);

        PreparationReminderCreationResult result =
                notificationReminderService.createPreparationReminderNotifications(executionTime);

        assertThat(result.targetCount()).isEqualTo(2);
        assertThat(result.createdNotificationCount()).isEqualTo(1);
        verify(notificationRepository).saveAll(List.of(newNotification));

        ArgumentCaptor<PolicyReminderNotificationsCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(PolicyReminderNotificationsCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().messages()).containsExactlyElementsOf(pushMessages);
    }

    @Test
    void doesNotSaveOrPublishWhenThereAreNoPreparationTargets() {
        LocalDateTime executionTime = LocalDateTime.of(2026, 8, 21, 10, 0);
        given(policyLikeRepository.findPreparationReminderTargets(
                LocalDateTime.of(2026, 8, 20, 9, 0),
                LocalDateTime.of(2026, 8, 20, 10, 0),
                RecruitmentStatus.CLOSED
        )).willReturn(List.of());

        PreparationReminderCreationResult result =
                notificationReminderService.createPreparationReminderNotifications(executionTime);

        assertThat(result.targetCount()).isZero();
        assertThat(result.createdNotificationCount()).isZero();
        verifyNoInteractions(notificationRepository, notificationReminderMapper, eventPublisher);
    }

    private PolicyLike policyLike(LocalDateTime likedAt, LocalDate applyEndDate) {
        Policy policy = org.mockito.Mockito.mock(Policy.class);
        PolicyLike policyLike = org.mockito.Mockito.mock(PolicyLike.class);

        given(policy.getApplyEndDate()).willReturn(applyEndDate);
        given(policyLike.getPolicy()).willReturn(policy);
        if (applyEndDate != null) {
            given(policyLike.getCreatedAt()).willReturn(likedAt);
        }
        return policyLike;
    }

    private PolicyLike preparationTarget(Long userId, Long policyId) {
        Policy policy = org.mockito.Mockito.mock(Policy.class);
        PolicyLike policyLike = org.mockito.Mockito.mock(PolicyLike.class);

        given(policy.getId()).willReturn(policyId);
        given(policyLike.getUserId()).willReturn(userId);
        given(policyLike.getPolicy()).willReturn(policy);
        return policyLike;
    }

    private Notification preparationNotification(Long userId, Long policyId) {
        return Notification.create(
                userId,
                NotificationCategory.MY_POLICY,
                NotificationType.POLICY_PREPARATION,
                "찜한 정책, 미리 준비해볼까요?",
                "신청할 때 놓치는 내용이 없도록 필요한 정보와 신청 방법을 미리 살펴보세요.",
                policyId
        );
    }
}
