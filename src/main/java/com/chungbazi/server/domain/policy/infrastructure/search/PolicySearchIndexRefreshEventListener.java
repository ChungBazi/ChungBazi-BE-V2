package com.chungbazi.server.domain.policy.infrastructure.search;

import com.chungbazi.server.domain.policy.application.event.PolicySearchIndexRefreshEvent;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicyDisplayStatus;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicySearchIndexRefreshEventListener {

    private final PolicyRepository policyRepository;
    private final LucenePolicySearchIndex policySearchIndex;

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handle(PolicySearchIndexRefreshEvent event) {
        List<Policy> changedPolicies = policyRepository.findAllById(event.changedPolicyIds());
        List<PolicySearchDocument> activePolicies = changedPolicies.stream()
                .filter(this::isSearchable)
                .map(PolicySearchDocument::from)
                .toList();

        Set<Long> activePolicyIds = new HashSet<>();
        activePolicies.forEach(policy -> activePolicyIds.add(policy.policyId()));

        List<Long> removedPolicyIds = new ArrayList<>();
        for (Long policyId : event.changedPolicyIds()) {
            if (!activePolicyIds.contains(policyId)) {
                removedPolicyIds.add(policyId);
            }
        }

        try {
            policySearchIndex.synchronize(activePolicies, removedPolicyIds);
            log.info(
                    "정책 검색 인덱스 증분 갱신 완료. upserted={}, removed={}",
                    activePolicies.size(),
                    removedPolicyIds.size()
            );
        } catch (IOException exception) {
            // 인덱스 갱신 실패가 이미 완료된 정책 동기화 결과를 되돌리지 않도록 예외 전파 X
            log.warn("정책 검색 인덱스 증분 갱신 실패", exception);
        }
    }

    private boolean isSearchable(Policy policy) {
        return policy.getDisplayStatus() == PolicyDisplayStatus.VISIBLE
                && policy.getRecruitmentStatus() != RecruitmentStatus.CLOSED;
    }
}
