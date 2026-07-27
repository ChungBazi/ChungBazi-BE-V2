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

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void syncPoliciesEveryDay() {
        SyncResult result = youthPolicySyncService.syncPolicies();
        log.info(
                "Youth policy sync finished. fetched={}, inserted={}, updated={}, unchanged={}, skipped={}",
                result.fetchedCount(),
                result.insertedCount(),
                result.updatedCount(),
                result.unchangedCount(),
                result.skippedCount()
        );
    }
}
