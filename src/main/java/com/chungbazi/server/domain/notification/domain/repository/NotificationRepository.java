package com.chungbazi.server.domain.notification.domain.repository;

import com.chungbazi.server.domain.notification.domain.Notification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

    boolean existsByUserIdAndReadFalse(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Notification notification
            SET notification.read = true
            WHERE notification.userId = :userId
              AND notification.id IN :notificationIds
              AND notification.read = false
            """)
    int markAllAsRead(
            @Param("userId") Long userId,
            @Param("notificationIds") Collection<Long> notificationIds
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Notification notification
            SET notification.read = true
            WHERE notification.userId = :userId
              AND notification.id = :notificationId
            """)
    int markAsRead(
            @Param("userId") Long userId,
            @Param("notificationId") Long notificationId
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM Notification notification
            WHERE notification.userId = :userId
              AND notification.id = :notificationId
            """)
    int deleteByUserIdAndNotificationId(
            @Param("userId") Long userId,
            @Param("notificationId") Long notificationId
    );
}
