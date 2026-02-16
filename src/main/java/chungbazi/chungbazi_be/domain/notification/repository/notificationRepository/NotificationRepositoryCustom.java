package chungbazi.chungbazi_be.domain.notification.repository.notificationRepository;


import chungbazi.chungbazi_be.domain.notification.dto.response.NotificationResponseDTO;
import chungbazi.chungbazi_be.domain.notification.entity.enums.NotificationType;


import java.util.List;


public interface NotificationRepositoryCustom {

    void markAllAsRead(Long userId,NotificationType type);

    List<NotificationResponseDTO.notificationsDto> findNotificationsByUserIdAndNotificationTypeDto(Long userId, NotificationType type, Long cursor, int limit);

}
