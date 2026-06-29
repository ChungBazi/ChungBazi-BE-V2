package com.chungbazi.server.domain.user.api.dto;

import com.chungbazi.server.domain.policy.domain.type.EducationCode;
import com.chungbazi.server.domain.policy.domain.type.EmploymentCode;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import com.chungbazi.server.domain.user.domain.type.IncomeLevel;
import lombok.Builder;

import java.util.Set;

@Builder
public record UserPolicyRequest(
        String birth,
        SidoCode sidoCode,
        String sigunguCode,
        EducationCode educationCode,
        EmploymentCode employmentCode,
        IncomeLevel incomeLevel,
        Set<PolicySubCategoryType> interestCategories
) {
}
