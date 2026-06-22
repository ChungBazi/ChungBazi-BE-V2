package com.chungbazi.server.domain.policy.domain.type.internal;

import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentType;

import java.time.LocalDate;

public record ApplyPeriod(
        LocalDate startDate,
        LocalDate endDate,
        String periodText,
        RecruitmentType recruitmentType,
        RecruitmentStatus recruitmentStatus
) {
}
