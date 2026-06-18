package com.chungbazi.server.domain.policy.dto.internal;

import com.chungbazi.server.domain.policy.enums.RecruitmentStatus;
import com.chungbazi.server.domain.policy.enums.RecruitmentType;
import java.time.LocalDate;

public record ApplyPeriod(
        LocalDate startDate,
        LocalDate endDate,
        String periodText,
        RecruitmentType recruitmentType,
        RecruitmentStatus recruitmentStatus
) {
}
