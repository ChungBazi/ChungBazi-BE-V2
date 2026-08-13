package com.chungbazi.server.domain.notification.application;

import com.chungbazi.server.domain.notification.api.dto.response.NotificationListResponse;
import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.repository.NotificationRepository;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
import com.chungbazi.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationListResponse getNotifications(
            User user,
            NotificationCategory category,
            Long cursor,
            int size
    ) {
        PageRequest pageRequest = PageRequest.of(0, size + 1);

        List<Notification> notifications = notificationRepository.findNotifications(
                user.getId(),
                category,
                cursor,
                pageRequest
        );

        boolean hasNext = notifications.size() > size;
        List<Notification> pagedNotifications = hasNext
                ? new ArrayList<>(notifications.subList(0, size))
                : notifications;
        Long nextCursor = hasNext
                ? pagedNotifications.getLast().getId()
                : null;

        return NotificationListResponse.of(
                pagedNotifications,
                nextCursor,
                hasNext,
                LocalDateTime.now()
        );
    }
}
