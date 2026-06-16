package com.chungbazi.server.domain.policy.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EducationCode {
    LESS_THAN_HIGH_SCHOOL("고졸 미만"),
    HIGH_SCHOOL_ENROLLED("고교 재학"),
    HIGH_SCHOOL_EXPECTED("고졸 예정"),
    HIGH_SCHOOL_GRADUATED("고교 졸업"),
    UNIVERSITY_ENROLLED("대학 재학"),
    UNIVERSITY_EXPECTED("대졸 예정"),
    UNIVERSITY_GRADUATED("대학 졸업"),
    MASTER_DOCTOR("석·박사"),
    ETC("기타"),
    NO_LIMIT("제한없음");

    private final String description;
}
