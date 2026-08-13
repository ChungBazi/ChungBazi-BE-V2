package com.chungbazi.server.domain.notification.domain;

import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
import com.chungbazi.server.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "notification",
        indexes = {
                @Index(
                        name = "idx_notification_user_id",
                        columnList = "user_id,notification_id"
                ),
                @Index(
                        name = "idx_notification_user_category_id",
                        columnList = "user_id,notification_category,notification_id"
                ),
                @Index(
                        name = "idx_notification_user_read",
                        columnList = "user_id,is_read"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_category", nullable = false, length = 20)
    private NotificationCategory category;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "policy_id")
    private Long policyId;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    public static Notification create(
            Long userId,
            NotificationCategory category,
            String title,
            String message,
            Long policyId
    ) {
        Notification notification = new Notification();
        notification.userId = userId;
        notification.category = category;
        notification.title = title;
        notification.message = message;
        notification.policyId = policyId;
        notification.read = false;
        return notification;
    }
}
