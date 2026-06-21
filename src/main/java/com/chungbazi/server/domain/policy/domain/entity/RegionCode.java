package com.chungbazi.server.domain.policy.domain.entity;

import com.chungbazi.server.domain.policy.infrastructure.persistence.converter.SidoCodeConverter;
import com.chungbazi.server.domain.policy.domain.vo.SidoCode;
import com.chungbazi.server.domain.policy.domain.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.domain.exception.PolicyException;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "region_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionCode {

    @Id
    @Column(name = "sigungu_code", length = 5)
    private String sigunguCode;

    @Column(name = "sigungu_name", nullable = false, length = 50)
    private String sigunguName;

    @Convert(converter = SidoCodeConverter.class)
    @Column(name = "sido_code", nullable = false, length = 2)
    private SidoCode sidoCode;

    public static RegionCode createRegionCode(
            String sigunguCode,
            String sigunguName,
            SidoCode sidoCode
    ) {
        if (sigunguCode == null
                || sigunguCode.length() != 5
                || !sigunguCode.chars().allMatch(Character::isDigit)) {
            throw new PolicyException(PolicyErrorCode.INVALID_POLICY_REGION);
        }

        SidoCode derivedSidoCode = SidoCode.fromSigunguCode(sigunguCode);
        if (sidoCode == null || sidoCode != derivedSidoCode) {
            throw new PolicyException(PolicyErrorCode.REGION_CODE_MISMATCH);
        }

        RegionCode regionCode = new RegionCode();
        regionCode.sigunguCode = sigunguCode;
        regionCode.sigunguName = sigunguName;
        regionCode.sidoCode = sidoCode;
        return regionCode;
    }
}
