package com.chungbazi.server.domain.notification.domain.repository;

import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationRepositoryCustom {

    List<Notification> findNotifications(
            Long userId,
            NotificationCategory category,
            Long cursor,
            Pageable pageable
    );
}
