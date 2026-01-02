package chungbazi.chungbazi_be.domain.notification.repository.notificationRepository;

import chungbazi.chungbazi_be.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {
}
