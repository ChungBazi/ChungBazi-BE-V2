package com.chungbazi.server.domain.notification.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationCategory {
    MY_POLICY("내 정책"),
    CHUNGBAZI("청바지");

    private final String description;
}
