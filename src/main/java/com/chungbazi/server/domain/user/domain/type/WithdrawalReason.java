package com.chungbazi.server.domain.user.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WithdrawalReason {
    POLICY_DISCOVERY_DIFFICULT("원하는 정책을 찾기 어려워요"),
    INSUFFICIENT_POLICY_INFORMATION("저에게 맞는 정책 추천이 부족해요"),
    NO_LONGER_NEEDED("이용할 일이 없어졌어요"),
    INCONVENIENT_APP("앱 사용이 불편했어요"),
    FREQUENT_ERRORS("오류가 자주 발생했어요"),
    OTHER("기타 이유가 있어요");

    private final String description;
}
