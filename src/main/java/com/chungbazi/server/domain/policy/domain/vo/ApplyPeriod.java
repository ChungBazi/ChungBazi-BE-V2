package com.chungbazi.server.domain.policy.domain.vo;

import com.chungbazi.server.domain.policy.domain.vo.RecruitmentStatus;
import com.chungbazi.server.domain.policy.domain.vo.RecruitmentType;
import java.time.LocalDate;

public record ApplyPeriod(
        LocalDate startDate,
        LocalDate endDate,
        String periodText,
        RecruitmentType recruitmentType,
        RecruitmentStatus recruitmentStatus
) {
}
