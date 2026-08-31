package com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper;

import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.domain.entity.PolicySpecialEligibility;
import com.chungbazi.server.domain.policy.domain.type.internal.ApplyPeriod;
import com.chungbazi.server.domain.policy.domain.type.internal.IncomeCondition;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyDetail;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import java.time.LocalDateTime;
import java.util.List;
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
            String plcyNo,
            PolicySubCategoryType subCategory,
            boolean national
    ) {
        ApplyPeriod applyPeriod = dateMapper.toApplyPeriod(item);
        IncomeCondition incomeCondition = incomeMapper.toIncomeCondition(item);

        return Policy.createPolicy(
                plcyNo,
                YouthPolicyTextUtils.trimToNull(item.plcyNm()),
                YouthPolicyTextUtils.trimToNull(item.plcyExplnCn()),
                YouthPolicyTextUtils.trimToNull(item.plcySprtCn()),
                YouthPolicyTextUtils.trimToNull(item.aplyUrlAddr()),
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
                dateMapper.toRegisteredAt(item.frstRegDt()),
                dateMapper.toSourceModifiedAt(item)
        );
    }

    public void updatePolicy(
            Policy policy,
            YouthPolicyItem item,
            PolicySubCategoryType subCategory,
            boolean national
    ) {
        ApplyPeriod applyPeriod = dateMapper.toApplyPeriod(item);
        IncomeCondition incomeCondition = incomeMapper.toIncomeCondition(item);

        policy.updatePolicy(
                YouthPolicyTextUtils.trimToNull(item.plcyNm()),
                YouthPolicyTextUtils.trimToNull(item.plcyExplnCn()),
                YouthPolicyTextUtils.trimToNull(item.plcySprtCn()),
                YouthPolicyTextUtils.trimToNull(item.aplyUrlAddr()),
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
                dateMapper.toSourceModifiedAt(item)
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
                null,
                YouthPolicyTextUtils.trimToNull(item.refUrlAddr1()),
                YouthPolicyTextUtils.trimToNull(item.refUrlAddr2())
        );
    }

    public void updatePolicyDetail(PolicyDetail policyDetail, YouthPolicyItem item) {
        policyDetail.updatePolicyDetail(
                toEligibilityDescription(item),
                YouthPolicyTextUtils.trimToNull(item.plcyAplyMthdCn()),
                YouthPolicyTextUtils.trimToNull(item.sbmsnDcmntCn()),
                YouthPolicyTextUtils.trimToNull(item.srngMthdCn()),
                YouthPolicyTextUtils.trimToNull(item.etcMttrCn()),
                null,
                YouthPolicyTextUtils.trimToNull(item.refUrlAddr1()),
                YouthPolicyTextUtils.trimToNull(item.refUrlAddr2())
        );
    }

    public List<PolicySpecialEligibility> toPolicySpecialEligibilities(Policy policy, YouthPolicyItem item) {
        return codeMapper.toSpecialEligibilityTypes(item.sbizCd()).stream()
                .map(eligibilityType -> PolicySpecialEligibility.create(policy, eligibilityType))
                .toList();
    }

    public LocalDateTime toSourceModifiedAt(YouthPolicyItem item) {
        return dateMapper.toSourceModifiedAt(item);
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
