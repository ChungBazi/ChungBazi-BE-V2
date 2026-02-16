package chungbazi.chungbazi_be.domain.notification.service;

import chungbazi.chungbazi_be.domain.notification.dto.internal.FcmPushData;
import chungbazi.chungbazi_be.domain.notification.dto.internal.NotificationData;
import chungbazi.chungbazi_be.domain.notification.dto.response.NotificationResponseDTO;
import chungbazi.chungbazi_be.domain.notification.entity.Notification;
import chungbazi.chungbazi_be.domain.notification.entity.enums.NotificationType;
import chungbazi.chungbazi_be.domain.notification.repository.notificationRepository.NotificationRepository;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.NotFoundHandler;
import chungbazi.chungbazi_be.global.utils.PaginationResult;
import chungbazi.chungbazi_be.global.utils.PaginationUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final FcmTokenService fcmTokenService;
    private final FcmService fcmService;
    private final UserHelper userHelper;

    //알람 읽음 처리
    @Transactional
    public void markAsRead(Long notificationId){
        Notification notification=notificationRepository.findById(notificationId)
                .orElseThrow(()->new NotFoundHandler(ErrorStatus.NOT_FOUND_NOTIFICATION));
        notification.markAsRead();
    }

    //알림 조회
    @Transactional
    public PaginationResult<NotificationResponseDTO.notificationsDto> getNotifications(NotificationType type, Long cursor, int limit) {

        User user = userHelper.getAuthenticatedUser();

        //알림 읽음 처리
        notificationRepository.markAllAsRead(user.getId(), type);

        List<NotificationResponseDTO.notificationsDto> notificationDtos = notificationRepository.findNotificationsByUserIdAndNotificationTypeDto(user.getId(), type, cursor, limit + 1);

        PaginationResult<NotificationResponseDTO.notificationsDto> paginationResult = PaginationUtil.paginate(notificationDtos, limit);

        return paginationResult;

    }

    @Transactional
    public void sendNotification(NotificationData request) {

        //알림 생성
        Notification notification= Notification.builder()
                .user(request.getUser())
                .message(request.getMessage())
                .targetId(request.getTargetId())
                .isRead(false)
                .type(request.getType())
                .build();

        notificationRepository.save(notification);

        //FCM 푸시 전송
        String fcmToken = fcmTokenService.getFcmToken(request.getUser().getId());

        if (fcmToken != null) {
            FcmPushData data = FcmPushData.from(notification);
            fcmService.pushFCMNotification(fcmToken,data);
        }
    }

    //안 읽은 알림이 있는지 검사하는 로직
    public boolean isReadAllNotification(User user) {
        return user.getNotificationList().stream()
                .allMatch(Notification::isRead);
    }

}
