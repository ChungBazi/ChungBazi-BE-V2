package com.chungbazi.server.domain.policy.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmploymentCode {
    EMPLOYED("재직자"),
    SELF_EMPLOYED("자영업자"),
    UNEMPLOYED("미취업자"),
    FREELANCER("프리랜서"),
    DAILY_WORKER("일용근로자"),
    PRE_FOUNDER("예비/창업자"),
    SHORT_TERM_WORKER("단기근로자"),
    FARMER("영농종사자"),
    ETC("기타"),
    NO_LIMIT("제한없음");

    private final String description;
}
