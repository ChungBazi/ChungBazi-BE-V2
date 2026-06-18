package com.chungbazi.server.domain.policy.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecruitmentType {
    FIXED_PERIOD("특정 기간 모집"),
    ALWAYS("상시 모집");

    private final String description;
}
