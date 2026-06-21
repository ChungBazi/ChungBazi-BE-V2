package com.chungbazi.server.domain.policy.domain.vo;

public record ParsedPeriod(
        DateRange dateRange,
        String periodText
) {
}
