package chungbazi.chungbazi_be.domain.notification.entity;

import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSetting extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private boolean policyAlarm=true;

    @Builder.Default
    private boolean communityAlarm=true;

    @Builder.Default
    private boolean rewardAlarm=true;

    @Builder.Default
    private boolean noticeAlarm=true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    public void updateNotificationSetting(boolean policyAlarm, boolean communityAlarm, boolean rewardAlarm, boolean noticeAlarm) {
        this.policyAlarm = policyAlarm;
        this.communityAlarm = communityAlarm;
        this.rewardAlarm = rewardAlarm;
        this.noticeAlarm = noticeAlarm;
    }
}
