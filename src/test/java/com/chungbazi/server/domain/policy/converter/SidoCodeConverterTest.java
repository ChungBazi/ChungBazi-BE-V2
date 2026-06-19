package com.chungbazi.server.domain.policy.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chungbazi.server.domain.policy.enums.SidoCode;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import org.junit.jupiter.api.Test;

class SidoCodeConverterTest {

    private final SidoCodeConverter converter = new SidoCodeConverter();

    @Test
    void storesAndRestoresAdministrativeCode() {
        assertThat(converter.convertToDatabaseColumn(SidoCode.SEOUL)).isEqualTo("11");
        assertThat(converter.convertToEntityAttribute("11")).isEqualTo(SidoCode.SEOUL);
    }

    @Test
    void rejectsUnsupportedSidoCodeWithPolicyException() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("99"))
                .isInstanceOf(PolicyException.class)
                .extracting(exception -> ((PolicyException) exception).getCode())
                .isEqualTo(PolicyErrorCode.INVALID_SIDO_CODE);
    }
}
