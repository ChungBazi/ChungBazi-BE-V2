package com.chungbazi.server.domain.policy.infrastructure.persistence.converter;

import com.chungbazi.server.domain.policy.domain.vo.SidoCode;
import com.chungbazi.server.domain.policy.domain.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.domain.exception.PolicyException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SidoCodeConverter implements AttributeConverter<SidoCode, String> {

    @Override
    public String convertToDatabaseColumn(SidoCode attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public SidoCode convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        SidoCode sidoCode = SidoCode.fromCode(dbData);
        if (sidoCode == null) {
            throw new PolicyException(PolicyErrorCode.INVALID_SIDO_CODE);
        }
        return sidoCode;
    }
}
