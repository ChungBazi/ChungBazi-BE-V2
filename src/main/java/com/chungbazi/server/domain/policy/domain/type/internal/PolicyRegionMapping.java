package com.chungbazi.server.domain.policy.domain.type.internal;

import java.util.List;

public record PolicyRegionMapping(
        boolean national,
        List<RegionScope> scopes
) {
    public PolicyRegionMapping {
        scopes = List.copyOf(scopes);
    }
}
