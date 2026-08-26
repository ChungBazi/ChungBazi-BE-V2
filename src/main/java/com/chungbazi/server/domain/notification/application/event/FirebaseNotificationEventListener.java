package com.chungbazi.server.domain.notification.application.event;

import com.chungbazi.server.domain.notification.infrastructure.fcm.FirebaseNotificationService;
import com.chungbazi.server.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "true")
public class FirebaseNotificationEventListener {

    private final FirebaseNotificationService firebaseNotificationService;

    @Async(AsyncConfig.NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PolicyReminderNotificationsCreatedEvent event) {
        try {
            firebaseNotificationService.sendPolicyReminders(event);
        } catch (RuntimeException exception) {
            log.error("비동기 FCM 정책 리마인드 발송 중 오류 발생", exception);
        }
    }

    @Async(AsyncConfig.NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PolicyUpdateNotificationsCreatedEvent event) {
        try {
            firebaseNotificationService.sendPolicyUpdates(event);
        } catch (RuntimeException exception) {
            log.error("비동기 FCM 찜한 정책 정보 변경 알림 발송 중 오류 발생", exception);
        }
    }

    @Async(AsyncConfig.NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void handle(PersonalizedPolicyNotificationsCreatedEvent event) {
        try {
            firebaseNotificationService.sendPersonalizedPolicies(event);
        } catch (RuntimeException exception) {
            log.error("비동기 FCM 신규 맞춤 정책 알림 발송 중 오류 발생", exception);
        }
    }
}
