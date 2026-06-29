package com.chungbazi.server.domain.user.application.validator;

import com.chungbazi.server.domain.policy.domain.type.EducationCode;
import com.chungbazi.server.domain.policy.domain.type.EmploymentCode;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import com.chungbazi.server.domain.user.api.dto.UserOnboardingRequest;
import com.chungbazi.server.domain.user.api.dto.UserPolicyRequest;
import com.chungbazi.server.domain.user.domain.type.IncomeLevel;
import com.chungbazi.server.domain.user.exception.UserException;
import com.chungbazi.server.domain.user.exception.code.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private static final int MIN_INTEREST_CATEGORY_COUNT = 3;

    public void validateOnboarding(UserOnboardingRequest request) {
        validateName(request.name());
        validatePolicyProfile(
                request.birth(),
                request.sidoCode(),
                request.sigunguCode(),
                request.educationCode(),
                request.employmentCode(),
                request.incomeLevel(),
                request.interestCategories()
        );
    }

    public void validatePolicy(UserPolicyRequest request) {
        validatePolicyProfile(
                request.birth(),
                request.sidoCode(),
                request.sigunguCode(),
                request.educationCode(),
                request.employmentCode(),
                request.incomeLevel(),
                request.interestCategories()
        );
    }

    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new UserException(UserErrorCode.INVALID_USER_NAME);
        }
    }

    private void validatePolicyProfile(
            String birth,
            SidoCode sidoCode,
            String sigunguCode,
            EducationCode educationCode,
            EmploymentCode employmentCode,
            IncomeLevel incomeLevel,
            Set<PolicySubCategoryType> interestCategories
    ) {
        if (birth == null || birth.isBlank()) {
            throw new UserException(UserErrorCode.INVALID_BIRTH);
        }

        if (sidoCode == null) {
            throw new UserException(UserErrorCode.INVALID_SIDO_CODE);
        }

        if (sigunguCode == null || sigunguCode.isBlank()) {
            throw new UserException(UserErrorCode.INVALID_SIGUNGU_CODE);
        }

        if (educationCode == null) {
            throw new UserException(UserErrorCode.INVALID_EDUCATION_CODE);
        }

        if (employmentCode == null) {
            throw new UserException(UserErrorCode.INVALID_EMPLOYMENT_CODE);
        }

        if (incomeLevel == null) {
            throw new UserException(UserErrorCode.INVALID_INCOME_LEVEL);
        }

        if (interestCategories == null || interestCategories.size() < MIN_INTEREST_CATEGORY_COUNT) {
            throw new UserException(UserErrorCode.INVALID_INTEREST_CATEGORY_COUNT);
        }
    }
}
