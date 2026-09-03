package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.application.event.PolicySearchIndexRefreshEvent;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicyDisplayStatus;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PolicyCleanupService {

    private static final int EXPIRED_POLICY_RETENTION_DAYS = 7;

    private final PolicyRepository policyRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void hideExpiredPolicies(LocalDate today) {
        LocalDate thresholdDate = today.minusDays(EXPIRED_POLICY_RETENTION_DAYS);

        List<Long> policyIdsToHide = policyRepository.findExpiredPolicyIdsToHide(
                PolicyDisplayStatus.VISIBLE,
                RecruitmentStatus.CLOSED,
                thresholdDate
        );
        policyRepository.hideExpiredPolicies(
                PolicyDisplayStatus.VISIBLE,
                PolicyDisplayStatus.HIDDEN_EXPIRED,
                RecruitmentStatus.CLOSED,
                thresholdDate
        );

        if (!policyIdsToHide.isEmpty()) {
            eventPublisher.publishEvent(PolicySearchIndexRefreshEvent.of(policyIdsToHide));
        }
    }
}
