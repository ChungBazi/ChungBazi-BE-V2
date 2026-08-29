package com.chungbazi.server.domain.policy.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PolicyCategoryType {
    JOB_STARTUP("취업/창업"),
    HOUSING("월세/주거"),
    GROWTH("공부/성장"),
    LIFE_SUPPORT("생활지원"),
    ACTIVITY("활동/경험");

    private final String description;
}
