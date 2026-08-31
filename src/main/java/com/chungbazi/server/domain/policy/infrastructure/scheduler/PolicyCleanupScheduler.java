package com.chungbazi.server.domain.policy.infrastructure.scheduler;

import com.chungbazi.server.domain.policy.application.PolicyCleanupService;
import com.chungbazi.server.global.logging.ScheduledWorkflowMdc;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyCleanupScheduler {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final PolicyCleanupService policyCleanupService;

    @Scheduled(
            cron = "${policy.cleanup.cron:0 30 4 * * *}",
            zone = "${policy.cleanup.zone:Asia/Seoul}"
    )
    public void hideExpiredPoliciesEveryDay() {
        try (ScheduledWorkflowMdc ignored = ScheduledWorkflowMdc.start("policy-cleanup")) {
            try {
                log.info("마감 정책 숨김 처리 시작");
                policyCleanupService.hideExpiredPolicies(LocalDate.now(SERVICE_ZONE_ID));
                log.info("마감 정책 숨김 처리 완료");
            } catch (RuntimeException exception) {
                log.error("마감 정책 숨김 처리 실패", exception);
                throw exception;
            }
        }
    }
}
