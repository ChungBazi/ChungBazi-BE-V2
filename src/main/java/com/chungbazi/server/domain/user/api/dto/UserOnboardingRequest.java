package com.chungbazi.server.domain.user.api.dto;

import com.chungbazi.server.domain.policy.domain.type.EducationCode;
import com.chungbazi.server.domain.policy.domain.type.EmploymentCode;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import com.chungbazi.server.domain.user.domain.type.IncomeLevel;
import lombok.Builder;

import java.util.Set;

@Builder
public record UserOnboardingRequest(
        String name,
        String birth,
        SidoCode sidoCode,
        String sigunguCode,
        EducationCode educationCode,
        EmploymentCode employmentCode,
        IncomeLevel incomeLevel,
        Set<PolicySubCategoryType> interestCategories
) {
    public static UserOnboardingRequest of(
            String name,
            String bitrh,
            SidoCode sidoCode,
            String sigunguCode,
            EducationCode educationCode,
            EmploymentCode employmentCode,
            IncomeLevel incomeLevel,
            Set<PolicySubCategoryType> interestCategories
    ) {
        return UserOnboardingRequest.builder()
                .name(name)
                .birth(bitrh)
                .sidoCode(sidoCode)
                .sigunguCode(sigunguCode)
                .educationCode(educationCode)
                .employmentCode(employmentCode)
                .incomeLevel(incomeLevel)
                .interestCategories(interestCategories)
                .build();
    }
}
