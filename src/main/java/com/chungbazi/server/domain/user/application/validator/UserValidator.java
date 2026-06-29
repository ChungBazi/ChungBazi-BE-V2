package com.chungbazi.server.domain.user.application.validator;

import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.user.api.dto.UserOnboardingRequest;
import com.chungbazi.server.domain.user.api.dto.UserPolicyRequest;
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
        validateInterestCategories(request.interestCategories());
    }

    public void validatePolicy(UserPolicyRequest request) {
        validateInterestCategories(request.interestCategories());
    }

    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new UserException(UserErrorCode.INVALID_USER_NAME);
        }
    }

    private void validateInterestCategories(Set<PolicySubCategoryType> interestCategories) {
        if (interestCategories == null || interestCategories.size() < MIN_INTEREST_CATEGORY_COUNT) {
            throw new UserException(UserErrorCode.INVALID_INTEREST_CATEGORY_COUNT);
        }
    }
}
