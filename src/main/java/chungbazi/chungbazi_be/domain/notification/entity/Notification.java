package chungbazi.chungbazi_be.domain.notification.entity;

import chungbazi.chungbazi_be.global.utils.TimeFormatter;
import chungbazi.chungbazi_be.domain.notification.entity.enums.NotificationType;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(nullable = false)
    private boolean isRead;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(length = 1000, nullable = false)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "target_id")
    private Long targetId;

    public void markAsRead() {
        this.isRead = true;
    }
}
