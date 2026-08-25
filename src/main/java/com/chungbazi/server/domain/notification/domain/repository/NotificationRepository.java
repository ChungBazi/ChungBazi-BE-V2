package com.chungbazi.server.domain.notification.domain.repository;

import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.type.NotificationType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

    boolean existsByUserIdAndReadFalse(Long userId);

    List<Notification> findAllByUserIdInAndPolicyIdInAndTypeIn(
            Collection<Long> userIds,
            Collection<Long> policyIds,
            Collection<NotificationType> types
    );

    List<Notification> findAllByUserIdInAndPolicyIdAndTypeAndPolicySourceModifiedAt(
            Collection<Long> userIds,
            Long policyId,
            NotificationType type,
            LocalDateTime policySourceModifiedAt
    );

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

    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM Notification notification
            WHERE notification.userId = :userId
            """)
    int deleteAllByUserId(@Param("userId") Long userId);
}
