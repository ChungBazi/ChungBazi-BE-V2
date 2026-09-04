package com.chungbazi.server.domain.policy.infrastructure.search;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chungbazi.server.domain.policy.application.event.PolicySearchIndexRefreshEvent;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicyDisplayStatus;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.fixture.PolicyFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicySearchIndexRefreshEventListenerTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private LucenePolicySearchIndex policySearchIndex;

    private PolicySearchIndexRefreshEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new PolicySearchIndexRefreshEventListener(
                policyRepository,
                policySearchIndex
        );
    }

    @Test
    @DisplayName("활성 정책은 갱신하고 종료되거나 존재하지 않는 정책은 삭제한다")
    void synchronizesActiveAndRemovedPolicies() throws Exception {
        Policy activePolicy = PolicyFixture.policy()
                .id(1L)
                .title("활성 정책")
                .displayStatus(PolicyDisplayStatus.VISIBLE)
                .recruitmentStatus(RecruitmentStatus.OPEN)
                .build();

        Policy closedPolicy = PolicyFixture.policy()
                .id(2L)
                .title("종료 정책")
                .displayStatus(PolicyDisplayStatus.VISIBLE)
                .recruitmentStatus(RecruitmentStatus.CLOSED)
                .build();
        when(policyRepository.findAllById(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(activePolicy, closedPolicy));

        listener.handle(PolicySearchIndexRefreshEvent.of(List.of(1L, 2L, 3L)));

        verify(policySearchIndex).synchronize(
                List.of(PolicySearchDocument.from(activePolicy)),
                List.of(2L, 3L)
        );
    }
}
