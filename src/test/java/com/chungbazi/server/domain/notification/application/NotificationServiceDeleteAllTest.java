package com.chungbazi.server.domain.notification.application;

import com.chungbazi.server.domain.notification.domain.repository.NotificationRepository;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.type.SocialType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceDeleteAllTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void deletesAllNotificationsByUserId() {
        User user = User.create(
                "provider-id",
                SocialType.KAKAO,
                "user@example.com",
                "사용자",
                null
        );

        notificationService.deleteAllNotifications(user);

        verify(notificationRepository).deleteAllByUserId(user.getId());
    }
}
