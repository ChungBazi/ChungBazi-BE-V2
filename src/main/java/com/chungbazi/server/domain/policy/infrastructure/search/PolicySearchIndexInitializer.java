package com.chungbazi.server.domain.policy.infrastructure.search;

import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicySearchIndexInitializer {

    private final PolicyRepository policyRepository;
    private final LucenePolicySearchIndex policySearchIndex;

    @Transactional(readOnly = true)
    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        List<PolicySearchDocument> documents = policyRepository
                .findAllSearchablePolicies(RecruitmentStatus.CLOSED)
                .stream()
                .map(PolicySearchDocument::from)
                .toList();

        try {
            policySearchIndex.rebuild(documents);
            log.info("활성 정책 검색 인덱스 생성 완료. documentCount={}", policySearchIndex.documentCount());
        } catch (IOException exception) {
            log.warn("활성 정책 검색 인덱스 생성 실패. 검색 관심 점수를 사용하지 않습니다.", exception);
        }
    }
}
