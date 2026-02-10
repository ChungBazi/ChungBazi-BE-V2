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

    //새로운 정책들 DB에 저장
    @Scheduled(cron = "0 5 20  * * *", zone = "Asia/Seoul")
    @Transactional
    public void policyFetch() {
        log.info("✅ 정책 스케줄러 실행 시작!");

        List<Policy> policies = policyFetchService.fetchPolicies();
        policyService.savePolicies(policies);

        log.info("✅ 정책 스케줄러 실행 완료!");
    }

    //마감기한 지난 정책들 DB에서 삭제
    @Scheduled(cron = "0 1 0  * * *", zone = "Asia/Seoul")
    public void deletePolicy() {

        long deletedCount = policyService.deleteExpiredPolicies();

        log.info("마감된 정책 삭제 완료. 총 삭제 개수 : {}", deletedCount);
    }
}
