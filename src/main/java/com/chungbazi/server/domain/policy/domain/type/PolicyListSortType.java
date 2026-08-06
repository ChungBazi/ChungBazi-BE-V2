package com.chungbazi.server.domain.policy.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PolicyListSortType {
    LATEST("최신순", PolicySortType.LATEST),
    DEADLINE("마감순", PolicySortType.DEADLINE);

    private final String description;
    private final PolicySortType policySortType;
}
