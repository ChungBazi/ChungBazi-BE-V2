package com.chungbazi.server.domain.user.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IncomeLevel {
    UNKNOWN("잘 모르겠어요"),
    LEVEL_1("1분위"),
    LEVEL_2("2분위"),
    LEVEL_3("3분위"),
    LEVEL_4("4분위"),
    LEVEL_5("5분위"),
    LEVEL_6("6분위"),
    LEVEL_7("7분위"),
    LEVEL_8("8분위"),
    LEVEL_9("9분위"),
    LEVEL_10("10분위")
    ;

    private final String description;
}
