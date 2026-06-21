package com.chungbazi.server.domain.policy.infrastructure.scheduler;

import com.chungbazi.server.domain.policy.application.YouthPolicySyncService;
import com.chungbazi.server.domain.policy.application.dto.SyncResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class YouthPolicyScheduler {

    private final YouthPolicySyncService youthPolicySyncService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void syncPoliciesEveryDay() {
        SyncResult result = youthPolicySyncService.syncPolicies();
        log.info(
                "Youth policy sync finished. fetched={}, saved={}, skipped={}",
                result.fetchedCount(),
                result.savedCount(),
                result.skippedCount()
        );
    }
}
