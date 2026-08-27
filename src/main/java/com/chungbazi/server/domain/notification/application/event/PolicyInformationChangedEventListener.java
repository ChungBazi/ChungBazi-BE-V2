package com.chungbazi.server.domain.notification.application.event;

import com.chungbazi.server.domain.notification.application.PolicyUpdateNotificationService;
import com.chungbazi.server.domain.policy.application.event.PolicyInformationChangedEvent;
import com.chungbazi.server.global.config.AsyncConfig;
import com.chungbazi.server.global.logging.AsyncTaskMdc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyInformationChangedEventListener {

    private final PolicyUpdateNotificationService policyUpdateNotificationService;

    @Async(AsyncConfig.NOTIFICATION_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PolicyInformationChangedEvent event) {
        try (AsyncTaskMdc ignored = AsyncTaskMdc.start("policy-update-notification-create")) {
            try {
                policyUpdateNotificationService.createPolicyUpdateNotifications(event);
            } catch (RuntimeException exception) {
                log.error(
                        "찜한 정책 정보 변경 알림 생성 중 오류 발생. policyId={}",
                        event.policyId(),
                        exception
                );
            }
        }
    }
}
