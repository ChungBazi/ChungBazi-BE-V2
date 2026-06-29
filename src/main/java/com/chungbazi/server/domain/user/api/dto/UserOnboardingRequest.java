package com.chungbazi.server.domain.user.api.dto;

import com.chungbazi.server.domain.policy.domain.type.EducationCode;
import com.chungbazi.server.domain.policy.domain.type.EmploymentCode;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import com.chungbazi.server.domain.user.domain.type.IncomeLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;

@Builder
@Schema(description = "온보딩 API")
public record UserOnboardingRequest(
        @Schema(description = "사용자 이름", example = "주정빈")
        String name,

        @Schema(description = "생년월일", example = "2002-03-15")
        String birth,

        @Schema(description = "시도 코드", example = "SEOUL")
        SidoCode sidoCode,

        @Schema(description = "시군구 코드", example = "11110")
        String sigunguCode,

        @Schema(description = "학력 코드", example = "UNIVERSITY_GRADUATED")
        EducationCode educationCode,

        @Schema(description = "취업 상태 코드", example = "EMPLOYED")
        EmploymentCode employmentCode,

        @Schema(description = "소득 구간", example = "UNKNOWN")
        IncomeLevel incomeLevel,

        @Schema(
                description = "관심 정책 분야 목록. 3개 이상 선택해야 합니다.",
                example = "[\"EMPLOYMENT_PREPARATION\", \"WORK_LIFE\", \"STARTUP_BUSINESS\"]"
        )
        Set<PolicySubCategoryType> interestCategories
) {
}
