package com.chungbazi.server.domain.policy.dto.internal;

import java.time.LocalDate;

public record DateRange(
        LocalDate startDate,
        LocalDate endDate
) {
}
