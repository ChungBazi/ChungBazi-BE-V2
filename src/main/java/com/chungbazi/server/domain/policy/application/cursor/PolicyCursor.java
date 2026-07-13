package com.chungbazi.server.domain.policy.application.cursor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PolicyCursor(
        LocalDateTime registeredAt,
        LocalDate applyEndDate,
        Long policyId
) {
}
