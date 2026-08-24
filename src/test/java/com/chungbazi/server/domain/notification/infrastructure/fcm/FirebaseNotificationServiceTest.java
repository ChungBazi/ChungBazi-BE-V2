package com.chungbazi.server.domain.notification.infrastructure.fcm;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.chungbazi.server.domain.notification.application.dto.NotificationPushMessage;
import com.chungbazi.server.domain.notification.application.event.PolicyReminderNotificationsCreatedEvent;
import com.chungbazi.server.domain.notification.application.event.PolicyUpdateNotificationsCreatedEvent;
import com.chungbazi.server.domain.notification.domain.NotificationSetting;
import com.chungbazi.server.domain.notification.domain.repository.NotificationSettingRepository;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
import com.chungbazi.server.domain.notification.infrastructure.fcm.dto.FirebasePushResult;
import com.chungbazi.server.domain.notification.infrastructure.fcm.dto.FirebasePushTarget;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.infrastructure.UserRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FirebaseNotificationServiceTest {

    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FirebaseNotificationSender firebaseNotificationSender;

    @InjectMocks
    private FirebaseNotificationService firebaseNotificationService;

    @Test
    void sendsOnlyToPushEnabledUsersAndClearsInvalidTokens() {
        NotificationPushMessage enabledUserMessage = pushMessage(1L, 10L);
        NotificationPushMessage disabledUserMessage = pushMessage(2L, 20L);
        PolicyReminderNotificationsCreatedEvent event =
                PolicyReminderNotificationsCreatedEvent.of(List.of(
                        enabledUserMessage,
                        disabledUserMessage
                ));

        User enabledUser = org.mockito.Mockito.mock(User.class);
        NotificationSetting enabledSetting = org.mockito.Mockito.mock(NotificationSetting.class);
        given(enabledSetting.getUser()).willReturn(enabledUser);
        given(enabledUser.getId()).willReturn(1L);
        given(enabledUser.getFcmToken()).willReturn("invalid-token");
        given(notificationSettingRepository.findPolicyPushEnabledSettings(Set.of(1L, 2L)))
                .willReturn(List.of(enabledSetting));

        FirebasePushTarget enabledTarget = FirebasePushTarget.of(
                "invalid-token",
                enabledUserMessage
        );
        given(firebaseNotificationSender.sendAll(List.of(enabledTarget)))
                .willReturn(FirebasePushResult.of(0, 1, Set.of("invalid-token")));

        firebaseNotificationService.sendPolicyReminders(event);

        verify(firebaseNotificationSender).sendAll(List.of(enabledTarget));
        verify(userRepository).clearInvalidFcmTokens(Set.of("invalid-token"));
    }

    @Test
    void doesNotCallFirebaseWhenNoUserHasPushEnabled() {
        NotificationPushMessage message = pushMessage(1L, 10L);
        PolicyReminderNotificationsCreatedEvent event =
                PolicyReminderNotificationsCreatedEvent.of(List.of(message));
        given(notificationSettingRepository.findPolicyPushEnabledSettings(Set.of(1L)))
                .willReturn(List.of());

        firebaseNotificationService.sendPolicyReminders(event);

        verify(firebaseNotificationSender, never()).sendAll(org.mockito.ArgumentMatchers.anyList());
        verify(userRepository, never()).clearInvalidFcmTokens(org.mockito.ArgumentMatchers.anySet());
    }

    @Test
    void sendsPolicyUpdateNotificationToPushEnabledUsers() {
        NotificationPushMessage message = pushMessage(1L, 10L);
        PolicyUpdateNotificationsCreatedEvent event =
                PolicyUpdateNotificationsCreatedEvent.of(List.of(message));

        User enabledUser = org.mockito.Mockito.mock(User.class);
        NotificationSetting enabledSetting = org.mockito.Mockito.mock(NotificationSetting.class);
        given(enabledSetting.getUser()).willReturn(enabledUser);
        given(enabledUser.getId()).willReturn(1L);
        given(enabledUser.getFcmToken()).willReturn("fcm-token");
        given(notificationSettingRepository.findPolicyPushEnabledSettings(Set.of(1L)))
                .willReturn(List.of(enabledSetting));

        FirebasePushTarget target = FirebasePushTarget.of("fcm-token", message);
        given(firebaseNotificationSender.sendAll(List.of(target)))
                .willReturn(FirebasePushResult.of(1, 0, Set.of()));

        firebaseNotificationService.sendPolicyUpdates(event);

        verify(firebaseNotificationSender).sendAll(List.of(target));
    }

    private NotificationPushMessage pushMessage(Long userId, Long policyId) {
        return new NotificationPushMessage(
                null,
                userId,
                NotificationCategory.MY_POLICY,
                "찜한 정책, 미리 준비해볼까요?",
                "신청할 때 놓치는 내용이 없도록 필요한 정보와 신청 방법을 미리 살펴보세요.",
                policyId
        );
    }
}
