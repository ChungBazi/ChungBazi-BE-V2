package com.chungbazi.server.domain.notification.domain.repository;

import com.chungbazi.server.domain.notification.domain.NotificationSetting;
import com.chungbazi.server.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    Optional<NotificationSetting> findByUser(User user);
}
