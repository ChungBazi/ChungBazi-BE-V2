package com.chungbazi.server.domain.user.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EducationCode {
    HIGH_SCHOOL_ENROLLED("고등학교 재학"),
    HIGH_SCHOOL_GRADUATED("고등학교 졸업"),

    UNIVERSITY_ENROLLED("대학교 재학"),
    UNIVERSITY_GRADUATED("대학교 졸업"),

    GRADUATE_ENROLLED("대학원 재학"),
    GRADUATE_GRADUATED("대학원 졸업"),

    OTHER("기타");

    private final String description;
}
