package chungbazi.chungbazi_be.global.scheduler;

import chungbazi.chungbazi_be.domain.policy.entity.Policy;
import chungbazi.chungbazi_be.domain.policy.service.PolicyFetchService;
import chungbazi.chungbazi_be.domain.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyScheduler {

    private final PolicyFetchService policyFetchService;
    private final PolicyService policyService;

    @Scheduled(cron = "0 40 20  * * *", zone = "Asia/Seoul")
    @Transactional
    public void policyFetch() {
        log.info("✅ 정책 스케줄러 실행 시작!");

        List<Policy> policies = policyFetchService.fetchPolicies();
        policyService.savePolicies(policies);

        log.info("✅ 정책 스케줄러 실행 완료!");
    }
}
