package com.chungbazi.server.domain.policy.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmploymentCode {
    EMPLOYED("재직(정규직/계약직 포함)"),
    TEMPORARY_DAILY_WORKER("단기·일용 근로"),
    SELF_EMPLOYED("자영업/사업"),
    FREELANCER("프리랜서"),
    UNEMPLOYED("미취업자"),
    ETC_OR_NONE("기타 / 해당 없음"),
    NONE_RESTRICT("제한 없음")
    ;

    private final String description;
}
