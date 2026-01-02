package chungbazi.chungbazi_be.domain.notification.service;

import chungbazi.chungbazi_be.domain.notification.converter.NotificationFactory;
import chungbazi.chungbazi_be.domain.notification.dto.*;
import chungbazi.chungbazi_be.domain.notification.entity.Notification;
import chungbazi.chungbazi_be.domain.notification.entity.enums.NotificationType;
import chungbazi.chungbazi_be.domain.notification.repository.NotificationRepository;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.NotFoundHandler;
import chungbazi.chungbazi_be.global.utils.PaginationResult;
import chungbazi.chungbazi_be.global.utils.PaginationUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final FCMService fcmService;
    private final UserHelper userHelper;

    @Transactional
    public NotificationResponseDTO.responseDto sendNotification(NotificationRequest request) {
        request.validate();

        //알림 생성
        Notification notification= NotificationFactory.from(request);
        notificationRepository.save(notification);

        //FCM 푸시 전송
        sendFcmPushIfTokenExists(notification);

        return NotificationResponseDTO.responseDto.builder()
                .notificationId(notification.getId())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    //알람 읽음 처리
    public void markAsRead(Long notificationId){
        Notification notification=notificationRepository.findById(notificationId)
                .orElseThrow(()->new NotFoundHandler(ErrorStatus.NOT_FOUND_NOTIFICATION));
        notification.markAsRead();
    }

    //알림 조회
    public NotificationResponseDTO.notificationListDto getNotifications(NotificationType type, Long cursor, int limit) {

        User user = userHelper.getAuthenticatedUser();

        //알림 읽음 처리
        notificationRepository.markAllAsRead(user.getId(), type);

        List<NotificationResponseDTO.notificationsDto> notificationDtos = notificationRepository.findNotificationsByUserIdAndNotificationTypeDto(user.getId(), type, cursor, limit + 1);

        PaginationResult<NotificationResponseDTO.notificationsDto> paginationResult =
                PaginationUtil.paginate(notificationDtos, limit);

        return NotificationResponseDTO.notificationListDto.builder()
                .notifications(notificationDtos)
                .nextCursor(paginationResult.getNextCursor())
                .hasNext(paginationResult.isHasNext())
                .build();

    }

    //안 읽은 알림이 있는지 검사하는 로직
    public boolean isReadAllNotification(){
        User user=userHelper.getAuthenticatedUser();

        return user.getNotificationList().stream()
                .anyMatch(notification -> !notification.isRead());
    }

    private void sendFcmPushIfTokenExists(Notification notification) {
        String fcmToken = fcmService.getToken(notification.getUser().getId());

        if (fcmToken != null) {
            FcmPushData data = FcmPushData.from(notification);
            fcmService.pushFCMNotification(fcmToken,data);
        }
    }
}
