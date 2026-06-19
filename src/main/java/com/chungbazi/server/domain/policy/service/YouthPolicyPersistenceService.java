package com.chungbazi.server.domain.policy.service;

import com.chungbazi.server.domain.policy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.dto.internal.PolicyRegionMapping;
import com.chungbazi.server.domain.policy.entity.Policy;
import com.chungbazi.server.domain.policy.enums.PolicySubCategoryType;
import com.chungbazi.server.domain.policy.mapper.YouthPolicyCategoryMapper;
import com.chungbazi.server.domain.policy.mapper.YouthPolicyEntityMapper;
import com.chungbazi.server.domain.policy.mapper.YouthPolicyRegionMapper;
import com.chungbazi.server.domain.policy.repository.PolicyDetailRepository;
import com.chungbazi.server.domain.policy.repository.PolicyRegionRepository;
import com.chungbazi.server.domain.policy.repository.PolicyRepository;
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
        if (CLOSED_PERIOD_CODE.equals(applyPeriodCode)
                || item.plcyNo() == null
                || item.plcyNo().isBlank()
                || policyRepository.existsByPlcyNo(item.plcyNo())) {
            return false;
        }

        PolicyRegionMapping regionMapping = policyRegionMapper.toRegionMapping(item.zipCd());

        PolicySubCategoryType subCategory = policyCategoryMapper.toCategory(item);

        Policy policy = policyEntityMapper.toPolicy(item, subCategory, regionMapping.national());
        Policy savedPolicy = policyRepository.save(policy);

        policyDetailRepository.save(policyEntityMapper.toPolicyDetail(savedPolicy, item));
        policyRegionRepository.saveAll(policyRegionMapper.toPolicyRegions(savedPolicy, regionMapping));

        return true;
    }
}
