package com.chungbazi.server.domain.policy.mapper;

import com.chungbazi.server.domain.policy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.dto.internal.ApplyPeriod;
import com.chungbazi.server.domain.policy.dto.internal.IncomeCondition;
import com.chungbazi.server.domain.policy.entity.Policy;
import com.chungbazi.server.domain.policy.entity.PolicyDetail;
import com.chungbazi.server.domain.policy.enums.PolicySubCategoryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class YouthPolicyEntityMapper {

    private final YouthPolicyCodeMapper codeMapper;
    private final YouthPolicyDateMapper dateMapper;
    private final YouthPolicyIncomeMapper incomeMapper;

    public Policy toPolicy(
            YouthPolicyItem item,
            PolicySubCategoryType subCategory,
            boolean national
    ) {
        ApplyPeriod applyPeriod = dateMapper.toApplyPeriod(item);
        IncomeCondition incomeCondition = incomeMapper.toIncomeCondition(item);

        return Policy.createPolicy(
                YouthPolicyTextUtils.trimToNull(item.plcyNo()),
                YouthPolicyTextUtils.trimToNull(item.plcyNm()),
                YouthPolicyTextUtils.trimToNull(item.plcyExplnCn()),
                YouthPolicyTextUtils.trimToNull(item.plcySprtCn()),
                subCategory,
                national,
                applyPeriod.startDate(),
                applyPeriod.endDate(),
                applyPeriod.periodText(),
                applyPeriod.recruitmentType(),
                applyPeriod.recruitmentStatus(),
                YouthPolicyTextUtils.parseInteger(item.sprtTrgtMinAge()),
                YouthPolicyTextUtils.parseInteger(item.sprtTrgtMaxAge()),
                codeMapper.toEducationCode(item.schoolCd()),
                codeMapper.toEmploymentCode(item.jobCd()),
                incomeCondition.type(),
                incomeCondition.minIncome(),
                incomeCondition.maxIncome(),
                incomeCondition.description(),
                toOrganizationName(item),
                dateMapper.toRegisteredAt(item.frstRegDt())
        );
    }

    public PolicyDetail toPolicyDetail(Policy policy, YouthPolicyItem item) {
        return PolicyDetail.createPolicyDetail(
                policy,
                toEligibilityDescription(item),
                YouthPolicyTextUtils.trimToNull(item.plcyAplyMthdCn()),
                YouthPolicyTextUtils.trimToNull(item.sbmsnDcmntCn()),
                YouthPolicyTextUtils.trimToNull(item.srngMthdCn()),
                YouthPolicyTextUtils.trimToNull(item.etcMttrCn()),
                YouthPolicyTextUtils.trimToNull(item.aplyUrlAddr()),
                YouthPolicyTextUtils.trimToNull(item.refUrlAddr1()),
                YouthPolicyTextUtils.trimToNull(item.refUrlAddr2())
        );
    }

    private String toEligibilityDescription(YouthPolicyItem item) {
        return YouthPolicyTextUtils.joinNonBlank(
                "\n\n",
                item.addAplyQlfcCndCn(),
                item.ptcpPrpTrgtCn(),
                item.earnEtcCn()
        );
    }

    private String toOrganizationName(YouthPolicyItem item) {
        String supervisingOrgName = YouthPolicyTextUtils.trimToNull(item.sprvsnInstCdNm());
        if (supervisingOrgName != null) {
            return supervisingOrgName;
        }
        return YouthPolicyTextUtils.trimToNull(item.operInstCdNm());
    }
}
