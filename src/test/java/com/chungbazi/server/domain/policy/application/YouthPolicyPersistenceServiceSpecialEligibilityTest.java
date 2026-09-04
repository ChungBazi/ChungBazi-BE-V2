package com.chungbazi.server.domain.policy.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.chungbazi.server.domain.policy.application.dto.PolicySyncItemResult;
import com.chungbazi.server.domain.policy.application.dto.PolicySyncStatus;
import com.chungbazi.server.domain.policy.domain.entity.PolicySpecialEligibility;
import com.chungbazi.server.domain.policy.domain.entity.RegionCode;
import com.chungbazi.server.domain.policy.domain.repository.PolicySpecialEligibilityRepository;
import com.chungbazi.server.domain.policy.domain.repository.RegionCodeRepository;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.user.domain.type.SpecialEligibilityType;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "firebase.enabled=false")
@ActiveProfiles("test")
@Transactional
class YouthPolicyPersistenceServiceSpecialEligibilityTest {

    private static final String SEOUL_JONGNO_CODE = "11110";

    @Autowired
    private YouthPolicyPersistenceService youthPolicyPersistenceService;

    @Autowired
    private PolicySpecialEligibilityRepository policySpecialEligibilityRepository;

    @Autowired
    private RegionCodeRepository regionCodeRepository;

    @BeforeEach
    void setUp() {
        if (!regionCodeRepository.existsById(SEOUL_JONGNO_CODE)) {
            regionCodeRepository.save(RegionCode.createRegionCode(
                    SEOUL_JONGNO_CODE,
                    "종로구",
                    SidoCode.SEOUL
            ));
        }
    }

    @Test
    void savesSpecialEligibilityWhenNewPolicyIsSynced() {
        PolicySyncItemResult result = youthPolicyPersistenceService.syncPolicy(
                item("policy-1", "0014002", "2026-01-01 00:00:00")
        );

        assertThat(result.status()).isEqualTo(PolicySyncStatus.INSERTED);
        assertThat(findSpecialEligibilities(result.policyId()))
                .containsExactly(SpecialEligibilityType.WOMAN);
    }

    @Test
    void savesMultipleSpecialEligibilitiesWhenNewPolicyIsSynced() {
        PolicySyncItemResult result = youthPolicyPersistenceService.syncPolicy(
                item("policy-2", "0014001,0014003", "2026-01-01 00:00:00")
        );

        assertThat(result.status()).isEqualTo(PolicySyncStatus.INSERTED);
        assertThat(findSpecialEligibilities(result.policyId()))
                .containsExactlyInAnyOrder(
                        SpecialEligibilityType.SME_EMPLOYEE,
                        SpecialEligibilityType.BASIC_LIVELIHOOD_RECIPIENT
                );
    }

    @Test
    void savesNoneWhenSpecialEligibilityCodeIsNull() {
        PolicySyncItemResult result = youthPolicyPersistenceService.syncPolicy(
                item("policy-3", null, "2026-01-01 00:00:00")
        );

        assertThat(result.status()).isEqualTo(PolicySyncStatus.INSERTED);
        assertThat(findSpecialEligibilities(result.policyId()))
                .containsExactly(SpecialEligibilityType.NONE);
    }

    @Test
    void replacesSpecialEligibilitiesWhenExistingPolicyIsUpdated() {
        PolicySyncItemResult inserted = youthPolicyPersistenceService.syncPolicy(
                item("policy-4", "0014002", "2026-01-01 00:00:00")
        );

        PolicySyncItemResult updated = youthPolicyPersistenceService.syncPolicy(
                item("policy-4", "0014006", "2026-01-02 00:00:00")
        );

        assertThat(updated.status()).isEqualTo(PolicySyncStatus.UPDATED);
        assertThat(updated.policyId()).isEqualTo(inserted.policyId());
        assertThat(findSpecialEligibilities(updated.policyId()))
                .containsExactly(SpecialEligibilityType.FARMER);
    }

    @Test
    void updatesSpecialEligibilitiesWhenExistingEligibilityIsIncludedAgain() {
        PolicySyncItemResult inserted = youthPolicyPersistenceService.syncPolicy(
                item("policy-5", "0014002", "2026-01-01 00:00:00")
        );

        PolicySyncItemResult updated = youthPolicyPersistenceService.syncPolicy(
                item("policy-5", "0014002,0014006", "2026-01-02 00:00:00")
        );

        assertThat(updated.status()).isEqualTo(PolicySyncStatus.UPDATED);
        assertThat(updated.policyId()).isEqualTo(inserted.policyId());
        assertThat(findSpecialEligibilities(updated.policyId()))
                .containsExactlyInAnyOrder(
                        SpecialEligibilityType.WOMAN,
                        SpecialEligibilityType.FARMER
                );
    }

    private Set<SpecialEligibilityType> findSpecialEligibilities(Long policyId) {
        return policySpecialEligibilityRepository.findAll().stream()
                .filter(eligibility -> eligibility.getPolicy().getId().equals(policyId))
                .map(PolicySpecialEligibility::getEligibilityType)
                .collect(Collectors.toSet());
    }

    private YouthPolicyItem item(
            String policyNumber,
            String specialEligibilityCode,
            String modifiedAt
    ) {
        return new YouthPolicyItem(
                policyNumber,
                "테스트 정책",
                "테스트",
                "정책 설명",
                "일자리",
                "취업",
                "지원 내용",
                null,
                "주관 기관",
                "운영 기관",
                "0057001",
                null,
                null,
                null,
                null,
                "신청 방법",
                "심사 방법",
                "https://example.com/apply",
                "제출 서류",
                "기타 사항",
                "https://example.com/ref1",
                null,
                "19",
                "39",
                "Y",
                null,
                "0043001",
                null,
                null,
                null,
                "추가 신청 자격",
                "참여 제한 대상",
                "0",
                SEOUL_JONGNO_CODE,
                null,
                "0013010",
                "0049010",
                "2026.01.01 ~ 2026.12.31",
                "2026-01-01 00:00:00",
                modifiedAt,
                specialEligibilityCode
        );
    }
}
