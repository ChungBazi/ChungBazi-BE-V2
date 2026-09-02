package com.chungbazi.server.domain.policy.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.chungbazi.server.domain.policy.application.event.PolicySearchIndexRefreshEvent;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicyDisplayStatus;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PolicyCleanupServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PolicyCleanupService policyCleanupService;

    @Test
    void hidesExpiredPoliciesIncludingSevenDayRetentionThreshold() {
        given(policyRepository.findExpiredPolicyIdsToHide(
                PolicyDisplayStatus.VISIBLE,
                RecruitmentStatus.CLOSED,
                LocalDate.of(2026, 8, 24)
        )).willReturn(List.of(10L, 20L));

        policyCleanupService.hideExpiredPolicies(LocalDate.of(2026, 8, 31));

        verify(policyRepository).findExpiredPolicyIdsToHide(
                PolicyDisplayStatus.VISIBLE,
                RecruitmentStatus.CLOSED,
                LocalDate.of(2026, 8, 24)
        );
        verify(policyRepository).hideExpiredPolicies(
                PolicyDisplayStatus.VISIBLE,
                PolicyDisplayStatus.HIDDEN_EXPIRED,
                RecruitmentStatus.CLOSED,
                LocalDate.of(2026, 8, 24)
        );

        ArgumentCaptor<PolicySearchIndexRefreshEvent> eventCaptor =
                ArgumentCaptor.forClass(PolicySearchIndexRefreshEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().changedPolicyIds())
                .containsExactly(10L, 20L);
    }
}
