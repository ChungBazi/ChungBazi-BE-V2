package com.chungbazi.server.domain.policy.infrastructure.scheduler;

import com.chungbazi.server.domain.policy.application.YouthPolicySyncService;
import com.chungbazi.server.domain.policy.application.dto.SyncResult;
import com.chungbazi.server.global.logging.ScheduledWorkflowMdc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class YouthPolicyScheduler {

    private final YouthPolicySyncService youthPolicySyncService;

    @Scheduled(
            cron = "${policy.sync.cron:0 0 4 * * *}",
            zone = "${policy.sync.zone:Asia/Seoul}"
    )
    public void syncPoliciesEveryDay() {
        try (ScheduledWorkflowMdc ignored = ScheduledWorkflowMdc.start("policy-sync")) {
            try {
                log.info("정책 동기화 시작");
                SyncResult result = youthPolicySyncService.syncPolicies();
                log.info(
                        "✅정책 동기화 완료. 읽은 정책 수={}, 새로 저장된 정책 수={}, 수정된 정책 수={}, 변경사항 없는 정책 수={}, 스킵된 정책 수={}, 전체 경과 시간={}",
                        result.fetchedCount(),
                        result.insertedCount(),
                        result.updatedCount(),
                        result.unchangedCount(),
                        result.skippedCount(),
                        result.totalElapsedMillis()
                );
            } catch (RuntimeException exception) {
                log.error("정책 동기화 실패", exception);
                throw exception;
            }
        }
    }
}
