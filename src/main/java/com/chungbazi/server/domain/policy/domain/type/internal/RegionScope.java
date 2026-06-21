package com.chungbazi.server.domain.policy.domain.type.internal;

import com.chungbazi.server.domain.policy.domain.entity.RegionCode;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;

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
