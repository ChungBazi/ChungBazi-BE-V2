package com.chungbazi.server.domain.policy.entity;

import com.chungbazi.server.domain.policy.enums.SidoCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    @Column(name = "sigungu_code", length = 10)
    private String sigunguCode;

    @Column(name = "sigungu_name", nullable = false, length = 50)
    private String sigunguName;

    @Enumerated(EnumType.STRING)
    @Column(name = "sido_code", nullable = false, length = 30)
    private SidoCode sidoCode;

    @Column(name = "sido_name", nullable = false, length = 50)
    private String sidoName;

    public static RegionCode createRegionCode(
            String sigunguCode,
            String sigunguName,
            SidoCode sidoCode
    ) {
        SidoCode derivedSidoCode = SidoCode.fromSigunguCode(sigunguCode);
        if (sidoCode == null || sidoCode != derivedSidoCode) {
            throw new IllegalArgumentException("시도 코드와 시군구 코드가 일치하지 않습니다.");
        }

        RegionCode regionCode = new RegionCode();
        regionCode.sigunguCode = sigunguCode;
        regionCode.sigunguName = sigunguName;
        regionCode.sidoCode = sidoCode;
        regionCode.sidoName = sidoCode.getName();
        return regionCode;
    }
}
