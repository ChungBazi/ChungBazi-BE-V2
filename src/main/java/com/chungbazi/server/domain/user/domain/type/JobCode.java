package com.chungbazi.server.domain.user.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobCode {
    EMPLOYEE("재직 중"),
    CONTRACT_WORKER("단기 일용직"),
    SELF_EMPLOYED("자영업"),
    FREELANCER("프리랜서"),
    UNEMPLOYED("미취업"),
    OTHER("기타");

    private final String description;
}
