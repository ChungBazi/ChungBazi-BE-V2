package com.chungbazi.server.domain.policy.application;

import static org.mockito.Mockito.verify;

import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicyDisplayStatus;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyCleanupServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private PolicyCleanupService policyCleanupService;

    @Test
    void hidesExpiredPoliciesIncludingSevenDayRetentionThreshold() {
        policyCleanupService.hideExpiredPolicies(LocalDate.of(2026, 8, 31));

        verify(policyRepository).hideExpiredPolicies(
                PolicyDisplayStatus.VISIBLE,
                PolicyDisplayStatus.HIDDEN_EXPIRED,
                RecruitmentStatus.CLOSED,
                LocalDate.of(2026, 8, 24)
        );
    }
}
