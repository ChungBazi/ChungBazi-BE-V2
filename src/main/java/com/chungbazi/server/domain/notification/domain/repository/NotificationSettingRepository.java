package com.chungbazi.server.domain.notification.domain.repository;

import com.chungbazi.server.domain.notification.domain.NotificationSetting;
import com.chungbazi.server.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    Optional<NotificationSetting> findByUser(User user);

    @Query("""
            SELECT setting
            FROM NotificationSetting setting
            JOIN FETCH setting.user user
            WHERE user.id IN :userIds
              AND user.deleted = false
              AND user.fcmToken IS NOT NULL
              AND setting.policyNotificationEnabled = true
            """)
    List<NotificationSetting> findPolicyPushEnabledSettings(
            @Param("userIds") Collection<Long> userIds
    );
}
