package com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import com.chungbazi.server.domain.user.domain.type.SpecialEligibilityType;
import org.junit.jupiter.api.Test;

class YouthPolicyCodeMapperTest {

    private final YouthPolicyCodeMapper youthPolicyCodeMapper = new YouthPolicyCodeMapper();

    @Test
    void mapsBlankSpecialEligibilityCodeToNone() {
        assertThat(youthPolicyCodeMapper.toSpecialEligibilityTypes(" "))
                .containsExactly(SpecialEligibilityType.NONE);
    }

    @Test
    void mapsEtcAndNoRestrictionSpecialEligibilityCodeToNone() {
        assertThat(youthPolicyCodeMapper.toSpecialEligibilityTypes("0014009,0014010"))
                .containsExactly(SpecialEligibilityType.NONE);
    }

    @Test
    void removesNoneWhenSpecificSpecialEligibilityCodeExists() {
        assertThat(youthPolicyCodeMapper.toSpecialEligibilityTypes("0014001,0014010"))
                .containsExactly(SpecialEligibilityType.SME_EMPLOYEE);
    }

    @Test
    void rejectsTrailingEmptySpecialEligibilityCode() {
        assertInvalidSpecialEligibilityCode("0014001,");
    }

    @Test
    void rejectsOnlyCommaSpecialEligibilityCode() {
        assertInvalidSpecialEligibilityCode(",");
    }

    private void assertInvalidSpecialEligibilityCode(String specialEligibilityCode) {
        assertThatThrownBy(() -> youthPolicyCodeMapper.toSpecialEligibilityTypes(specialEligibilityCode))
                .isInstanceOf(PolicyException.class)
                .extracting("code")
                .isEqualTo(PolicyErrorCode.INVALID_POLICY_SPECIAL_ELIGIBILITY);
    }
}
