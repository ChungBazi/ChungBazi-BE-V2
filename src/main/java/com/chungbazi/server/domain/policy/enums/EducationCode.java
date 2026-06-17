package com.chungbazi.server.domain.policy.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EducationCode {
    HIGH_SCHOOL_ATTENDING("고등학교 재학 중 (검정고시 포함)"),
    HIGH_SCHOOL_GRADUATED_NOT_ATTENDING("고등학교 졸업 후 미진학"),
    UNIVERSITY_ATTENDING_OR_ON_LEAVE("대학교 재학/휴학 중"),
    UNIVERSITY_GRADUATED("대학교 졸업 (학사 학위 취득자)"),
    GRADUATE_SCHOOL_ATTENDING_OR_COMPLETED("대학원 재학/휴학 중 또는 수료"),
    GRADUATE_SCHOOL_GRADUATED("대학원 졸업 (석·박사 학위 취득자)"),
    ETC_OR_NONE("기타 / 해당 없음"),
    NONE_RESCRICT("제한 없음")
    ;

    private final String description;
}
