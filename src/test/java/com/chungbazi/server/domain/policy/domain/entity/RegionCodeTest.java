package com.chungbazi.server.domain.policy.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import org.junit.jupiter.api.Test;

class RegionCodeTest {

    @Test
    void createsJeonnamGwangjuRegionCodeFromSigunguCode() {
        RegionCode regionCode = RegionCode.createRegionCode(
                "12110",
                "목포시",
                SidoCode.JEONNAM_GWANGJU
        );

        assertThat(regionCode.getSigunguCode()).isEqualTo("12110");
        assertThat(regionCode.getSigunguName()).isEqualTo("목포시");
        assertThat(regionCode.getSidoCode()).isEqualTo(SidoCode.JEONNAM_GWANGJU);
    }

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
