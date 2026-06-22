package com.chungbazi.server.domain.policy.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PolicySortType {
    LATEST("최신순"),
    DEADLINE("마감순");

    private final String description;
}
