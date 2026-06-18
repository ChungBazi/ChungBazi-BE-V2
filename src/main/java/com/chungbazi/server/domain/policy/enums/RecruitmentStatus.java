package com.chungbazi.server.domain.policy.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecruitmentStatus {
    OPEN("모집중"),
    UPCOMING("모집예정"),
    CLOSED("마감"),
    ALWAYS("상시모집"),
    UNKNOWN("알 수 없음");

    private final String description;
}
