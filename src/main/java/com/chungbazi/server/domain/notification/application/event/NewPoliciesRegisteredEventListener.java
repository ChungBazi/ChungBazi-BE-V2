package com.chungbazi.server.domain.notification.application.event;

import com.chungbazi.server.domain.notification.application.InterestPolicyNotificationService;
import com.chungbazi.server.domain.notification.application.PersonalizedPolicyNotificationService;
import com.chungbazi.server.domain.policy.application.event.NewPoliciesRegisteredEvent;
import com.chungbazi.server.global.config.AsyncConfig;
import com.chungbazi.server.global.logging.AsyncTaskMdc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewPoliciesRegisteredEventListener {

    private final PersonalizedPolicyNotificationService personalizedPolicyNotificationService;
    private final InterestPolicyNotificationService interestPolicyNotificationService;

    @Async(AsyncConfig.NOTIFICATION_EXECUTOR)
    @EventListener
    public void handle(NewPoliciesRegisteredEvent event) {
        try (AsyncTaskMdc ignored = AsyncTaskMdc.start("personalized-policy-notification-create")) {
            try {
                personalizedPolicyNotificationService.createPersonalizedPolicyNotifications(event);
                interestPolicyNotificationService.createInterestPolicyNotifications(event);
            } catch (RuntimeException exception) {
                log.error(
                        "신규 맞춤 정책 알림 생성 중 오류 발생. policyIds={}",
                        event.policyIds(),
                        exception
                );
            }
        }
    }
}
