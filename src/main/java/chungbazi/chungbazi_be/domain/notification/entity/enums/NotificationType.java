package chungbazi.chungbazi_be.domain.notification.entity.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    POLICY("정책 관련 알림"),
    COMMUNITY("커뮤니티 관련 알림"),
    REWARD("리워드 관련 알림"),
    NOTICE("공지 관련 알림"),
    CHAT("채팅 관련 알림");

    private final String description;

    NotificationType(String description){this.description=description;}
}
