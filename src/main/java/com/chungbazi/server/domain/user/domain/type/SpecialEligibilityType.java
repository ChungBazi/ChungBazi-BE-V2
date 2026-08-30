package com.chungbazi.server.domain.user.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpecialEligibilityType {

    WOMAN("여성"),
    BASIC_LIVELIHOOD_RECIPIENT("기초생활수급자"),
    PERSON_WITH_DISABILITY("장애인"),
    MILITARY_PERSONNEL("군인"),
    LOCAL_TALENT("지역 인재"),
    FARMER("농업인"),
    SINGLE_PARENT_FAMILY("한부모가정"),
    SME_EMPLOYEE("중소기업 재직"),
    NONE("해당 없음");

    private final String description;
}
