package com.chungbazi.server.domain.notification.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    POLICY_PREPARATION("찜한 정책 신청 준비"),
    POLICY_DEADLINE_D7("찜한 정책 신청 마감 7일 전"),
    POLICY_DEADLINE_D3("찜한 정책 신청 마감 3일 전"),
    POLICY_UPDATED("찜한 정책 정보 변경"),
    PERSONALIZED_POLICY("맞춤 정책 추천"),
    INTEREST_POLICY("관심 분야 신규 정책"),
    UNKNOWN("기존 알림");

    private final String description;
}
