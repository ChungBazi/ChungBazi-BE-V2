package com.chungbazi.server.domain.policy.domain.entity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chungbazi.server.domain.policy.domain.vo.SidoCode;
import com.chungbazi.server.domain.policy.domain.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.domain.exception.PolicyException;
import org.junit.jupiter.api.Test;

class RegionCodeTest {

    @Test
    void rejectsNonNumericSigunguCode() {
        assertThatThrownBy(() -> RegionCode.createRegionCode("11ABC", "잘못된 지역", SidoCode.SEOUL))
                .isInstanceOf(PolicyException.class)
                .extracting(exception -> ((PolicyException) exception).getCode())
                .isEqualTo(PolicyErrorCode.INVALID_POLICY_REGION);
    }

    @Test
    void rejectsSigunguCodeThatIsNotFiveDigits() {
        assertThatThrownBy(() -> RegionCode.createRegionCode("1101", "잘못된 지역", SidoCode.SEOUL))
                .isInstanceOf(PolicyException.class)
                .extracting(exception -> ((PolicyException) exception).getCode())
                .isEqualTo(PolicyErrorCode.INVALID_POLICY_REGION);
    }
}
