package com.chungbazi.server.domain.notification.infrastructure.fcm;

import com.chungbazi.server.domain.notification.application.dto.NotificationPushMessage;
import com.chungbazi.server.domain.notification.application.event.PolicyReminderNotificationsCreatedEvent;
import com.chungbazi.server.domain.notification.domain.NotificationSetting;
import com.chungbazi.server.domain.notification.domain.repository.NotificationSettingRepository;
import com.chungbazi.server.domain.notification.infrastructure.fcm.dto.FirebasePushResult;
import com.chungbazi.server.domain.notification.infrastructure.fcm.dto.FirebasePushTarget;
import com.chungbazi.server.domain.user.infrastructure.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "true")
public class FirebaseNotificationService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final UserRepository userRepository;
    private final FirebaseNotificationSender firebaseNotificationSender;

    public void sendPolicyReminders(PolicyReminderNotificationsCreatedEvent event) {
        Set<Long> userIds = event.messages().stream()
                .map(NotificationPushMessage::userId)
                .collect(Collectors.toSet());

        List<NotificationSetting> enabledSettings =
                notificationSettingRepository.findPolicyPushEnabledSettings(userIds);

        Map<Long, String> fcmTokenByUserId = enabledSettings.stream()
                .collect(Collectors.toMap(
                        setting -> setting.getUser().getId(),
                        setting -> setting.getUser().getFcmToken()
                ));

        List<FirebasePushTarget> targets = event.messages().stream()
                .filter(message -> fcmTokenByUserId.containsKey(message.userId()))
                .map(message -> FirebasePushTarget.of(
                        fcmTokenByUserId.get(message.userId()),
                        message
                ))
                .toList();

        if (targets.isEmpty()) {
            return;
        }

        FirebasePushResult result = firebaseNotificationSender.sendAll(targets);
        if (!result.invalidFcmTokens().isEmpty()) {
            userRepository.clearInvalidFcmTokens(result.invalidFcmTokens());
        }

        log.info(
                "FCM 정책 리마인드 발송 완료. 성공={}, 실패={}, 무효 토큰={}",
                result.successCount(),
                result.failureCount(),
                result.invalidFcmTokens().size()
        );
    }
}
