package com.chungbazi.server.domain.policy.dto.internal;

import com.chungbazi.server.domain.policy.entity.RegionCode;
import com.chungbazi.server.domain.policy.enums.SidoCode;

public record RegionScope(
        SidoCode sidoCode,
        RegionCode regionCode
) {
    public static RegionScope sido(SidoCode sidoCode) {
        return new RegionScope(sidoCode, null);
    }

    public static RegionScope sigungu(RegionCode regionCode) {
        return new RegionScope(regionCode.getSidoCode(), regionCode);
    }
}
