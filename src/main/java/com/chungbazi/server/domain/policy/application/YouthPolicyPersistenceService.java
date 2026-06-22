package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.domain.type.internal.PolicyRegionMapping;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
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
    public boolean saveIfNew(YouthPolicyItem item) {
        String applyPeriodCode = item.aplyPrdSeCd() == null ? null : item.aplyPrdSeCd().trim();
        String plcyNo = normalizePolicyNumber(item.plcyNo());
        if (CLOSED_PERIOD_CODE.equals(applyPeriodCode)
                || plcyNo == null
                || policyRepository.existsByPlcyNo(plcyNo)) {
            return false;
        }

        PolicyRegionMapping regionMapping = policyRegionMapper.toRegionMapping(item.zipCd());

        PolicySubCategoryType subCategory = policyCategoryMapper.toCategory(item);

        Policy policy = policyEntityMapper.toPolicy(item, plcyNo, subCategory, regionMapping.national());
        Policy savedPolicy = policyRepository.save(policy);

        policyDetailRepository.save(policyEntityMapper.toPolicyDetail(savedPolicy, item));
        policyRegionRepository.saveAll(policyRegionMapper.toPolicyRegions(savedPolicy, regionMapping));

        return true;
    }

    private String normalizePolicyNumber(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
