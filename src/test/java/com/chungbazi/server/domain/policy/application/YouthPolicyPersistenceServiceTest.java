package com.chungbazi.server.domain.policy.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.chungbazi.server.domain.policy.application.dto.PolicySyncItemResult;
import com.chungbazi.server.domain.policy.application.dto.PolicySyncStatus;
import com.chungbazi.server.domain.policy.domain.repository.PolicyDetailRepository;
import com.chungbazi.server.domain.policy.domain.repository.PolicyRegionRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentType;
import com.chungbazi.server.domain.policy.domain.type.internal.ApplyPeriod;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper.YouthPolicyCategoryMapper;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper.YouthPolicyDateMapper;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper.YouthPolicyEntityMapper;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper.YouthPolicyRegionMapper;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class YouthPolicyPersistenceServiceTest {

    @Mock
    private YouthPolicyEntityMapper policyEntityMapper;

    @Mock
    private YouthPolicyCategoryMapper policyCategoryMapper;

    @Mock
    private YouthPolicyDateMapper policyDateMapper;

    @Mock
    private YouthPolicyRegionMapper policyRegionMapper;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyDetailRepository policyDetailRepository;

    @Mock
    private PolicyRegionRepository policyRegionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private YouthPolicyPersistenceService youthPolicyPersistenceService;

    @Test
    void skipsNewPolicyWhenApplyPeriodIsAlreadyExpired() {
        YouthPolicyItem item = org.mockito.Mockito.mock(YouthPolicyItem.class);
        given(item.aplyPrdSeCd()).willReturn("0057001");
        given(item.plcyNo()).willReturn("policy-expired");
        given(policyDateMapper.toApplyPeriod(item))
                .willReturn(new ApplyPeriod(
                        LocalDate.of(2025, 1, 1),
                        LocalDate.of(2025, 12, 31),
                        "2025.01.01 ~ 2025.12.31",
                        RecruitmentType.FIXED_PERIOD,
                        RecruitmentStatus.CLOSED
                ));
        given(policyRepository.findByPlcyNo("policy-expired")).willReturn(Optional.empty());

        PolicySyncItemResult result = youthPolicyPersistenceService.syncPolicy(item);

        assertThat(result.status()).isEqualTo(PolicySyncStatus.SKIPPED_CLOSED);
        assertThat(result.policyId()).isNull();
        verify(policyRepository, never()).save(any());
        verify(policyRegionMapper, never()).toRegionMapping(any());
    }
}
