package com.chungbazi.server.domain.notification.domain;

import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "notification_setting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_setting_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "all_notification", nullable = false)
    private boolean allNotificationEnabled;

    @Column(name = "policy_notification", nullable = false)
    private boolean policyNotificationEnabled;

    @Column(name = "chungbazi_notification", nullable = false)
    private boolean chungbaziNotificationEnabled;

    public static NotificationSetting create(User user) {
        NotificationSetting setting = new NotificationSetting();
        setting.user = user;
        setting.allNotificationEnabled = true;
        setting.policyNotificationEnabled = true;
        setting.chungbaziNotificationEnabled = true;
        return setting;
    }

    public void updateNotificationSetting(
            boolean allNotificationEnabled,
            boolean policyNotificationEnabled,
            boolean chungbaziNotificationEnabled
    ) {
        this.allNotificationEnabled = allNotificationEnabled;
        this.policyNotificationEnabled = allNotificationEnabled && policyNotificationEnabled;
        this.chungbaziNotificationEnabled = allNotificationEnabled && chungbaziNotificationEnabled;
    }
}
