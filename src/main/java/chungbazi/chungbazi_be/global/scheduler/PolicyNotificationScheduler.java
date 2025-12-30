package chungbazi.chungbazi_be.global.scheduler;

import chungbazi.chungbazi_be.domain.cart.entity.Cart;
import chungbazi.chungbazi_be.domain.cart.service.CartService;
import chungbazi.chungbazi_be.domain.notification.dto.internal.NotificationData;
import chungbazi.chungbazi_be.domain.notification.entity.enums.NotificationType;
import chungbazi.chungbazi_be.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PolicyNotificationScheduler {
    private final CartService cartService;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 10 19 * * *")
    @Transactional
    public void sendReminderNotifications() {
        LocalDate threeDaysLater = LocalDate.now().plusDays(3);
        LocalDate oneDayLater = LocalDate.now().plusDays(1);

        List<LocalDate> targetDates = List.of(threeDaysLater, oneDayLater);

        // 3일 뒤 마감되는 정책을 장바구니에 담은 사용자 목록 조회
        List<Cart> carts = cartService.getCartsByEndDate(targetDates);

        // 알림 설정이 켜져 있는 사용자들에게 알림 발송
        for (Cart cart : carts) {
            if (cart.getUser().getNotificationSetting().isPolicyAlarm()) {
                LocalDate endDate = cart.getPolicy().getEndDate();
                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), endDate);

                String message = String.format("%s 정책이 %d일 뒤 마감됩니다!",
                        cart.getPolicy().getName(), daysLeft);

                NotificationData notificationData = NotificationData.builder()
                        .user(cart.getUser())
                        .type(NotificationType.POLICY)
                        .message(message)
                        .targetId(cart.getPolicy().getId())
                        .build();
                notificationService.sendNotification(notificationData);
            }
        }
    }
}
