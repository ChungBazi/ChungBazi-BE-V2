package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.application.dto.PolicySyncStatus;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.domain.type.internal.PolicyRegionMapping;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyDetail;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper.YouthPolicyCategoryMapper;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper.YouthPolicyEntityMapper;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper.YouthPolicyRegionMapper;
import com.chungbazi.server.domain.policy.domain.repository.PolicyDetailRepository;
import com.chungbazi.server.domain.policy.domain.repository.PolicyRegionRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class YouthPolicyPersistenceService {

    private static final String CLOSED_PERIOD_CODE = "0057003";

    private final YouthPolicyEntityMapper policyEntityMapper;
    private final YouthPolicyCategoryMapper policyCategoryMapper;
    private final YouthPolicyRegionMapper policyRegionMapper;
    private final PolicyRepository policyRepository;
    private final PolicyDetailRepository policyDetailRepository;
    private final PolicyRegionRepository policyRegionRepository;

    @Transactional
    public PolicySyncStatus syncPolicy(YouthPolicyItem item) {
        String applyPeriodCode = item.aplyPrdSeCd() == null ? null : item.aplyPrdSeCd().trim();
        String plcyNo = normalizePolicyNumber(item.plcyNo());
        if (plcyNo == null) {
            return PolicySyncStatus.SKIPPED;
        }

        return policyRepository.findByPlcyNo(plcyNo)
                .map(policy -> syncExistingPolicy(policy, item, applyPeriodCode))
                .orElseGet(() -> syncNewPolicy(item, plcyNo, applyPeriodCode));
    }

    private PolicySyncStatus syncNewPolicy(YouthPolicyItem item, String plcyNo, String applyPeriodCode) {
        if (CLOSED_PERIOD_CODE.equals(applyPeriodCode)) {
            return PolicySyncStatus.SKIPPED;
        }

        PolicyRegionMapping regionMapping = policyRegionMapper.toRegionMapping(item.zipCd());

        PolicySubCategoryType subCategory = policyCategoryMapper.toCategory(item);

        Policy policy = policyEntityMapper.toPolicy(item, plcyNo, subCategory, regionMapping.national());
        Policy savedPolicy = policyRepository.save(policy);

        policyDetailRepository.save(policyEntityMapper.toPolicyDetail(savedPolicy, item));
        policyRegionRepository.saveAll(policyRegionMapper.toPolicyRegions(savedPolicy, regionMapping));

        return PolicySyncStatus.INSERTED;
    }

    private PolicySyncStatus syncExistingPolicy(Policy policy, YouthPolicyItem item, String applyPeriodCode) {
        //업데이트가 필요한지 확인 (최종 수정일이 동기화한 날짜 이후인 경우)
        if (!shouldUpdate(policy.getSourceModifiedAt(), policyEntityMapper.toSourceModifiedAt(item))) {
            return PolicySyncStatus.UNCHANGED;
        }

        //마감된 정책인지 확인
        if (CLOSED_PERIOD_CODE.equals(applyPeriodCode)) {
            policy.updateRecruitmentStatus(RecruitmentStatus.CLOSED, policyEntityMapper.toSourceModifiedAt(item));
            return PolicySyncStatus.UPDATED;
        }

        PolicyRegionMapping regionMapping = policyRegionMapper.toRegionMapping(item.zipCd());
        PolicySubCategoryType subCategory = policyCategoryMapper.toCategory(item);

        policyEntityMapper.updatePolicy(policy, item, subCategory, regionMapping.national());
        syncPolicyDetail(policy, item);
        policyRegionRepository.deleteAllByPolicyId(policy.getId());
        policyRegionRepository.saveAll(policyRegionMapper.toPolicyRegions(policy, regionMapping));

        return PolicySyncStatus.UPDATED;
    }

    private boolean shouldUpdate(LocalDateTime originalModifiedAt, java.time.LocalDateTime sourceModifiedAt) {
        if (originalModifiedAt == null) {
            return true;
        }
        return sourceModifiedAt != null && sourceModifiedAt.isAfter(originalModifiedAt);
    }

    private void syncPolicyDetail(Policy policy, YouthPolicyItem item) {
        PolicyDetail policyDetail = policyDetailRepository.findByPolicyId(policy.getId())
                .orElseGet(() -> policyDetailRepository.save(policyEntityMapper.toPolicyDetail(policy, item)));

        policyEntityMapper.updatePolicyDetail(policyDetail, item);
    }

    private String normalizePolicyNumber(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
