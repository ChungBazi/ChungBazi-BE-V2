package com.chungbazi.server.domain.policy.domain.type.internal;

import java.time.LocalDate;

public record DateRange(
        LocalDate startDate,
        LocalDate endDate
) {
}
