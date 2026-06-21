package com.chungbazi.server.domain.policy.domain.vo;

import java.time.LocalDate;

public record DateRange(
        LocalDate startDate,
        LocalDate endDate
) {
}
