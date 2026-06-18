package com.chungbazi.server.domain.policy.dto.internal;

public record ParsedPeriod(
        DateRange dateRange,
        String periodText
) {
}
