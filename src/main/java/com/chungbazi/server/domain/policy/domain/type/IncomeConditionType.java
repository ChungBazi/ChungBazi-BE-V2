package com.chungbazi.server.domain.policy.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IncomeConditionType {
    NO_LIMIT("소득 제한 없음"),
    AMOUNT_BASED("연소득"),
    OTHER("기타")
    ;

    private final String description;
}