package com.chungbazi.server.domain.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.chungbazi.server.domain.notification.domain.repository.NotificationRepository;
import com.chungbazi.server.domain.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceUnreadStatusTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void returnsTrueWhenUnreadNotificationExists() {
        User user = org.mockito.Mockito.mock(User.class);
        given(user.getId()).willReturn(1L);
        given(notificationRepository.existsByUserIdAndReadFalse(1L)).willReturn(true);

        boolean result = notificationService.hasUnreadNotification(user);

        assertThat(result).isTrue();
        verify(notificationRepository).existsByUserIdAndReadFalse(1L);
    }

    @Test
    void returnsFalseWhenUnreadNotificationDoesNotExist() {
        User user = org.mockito.Mockito.mock(User.class);
        given(user.getId()).willReturn(1L);
        given(notificationRepository.existsByUserIdAndReadFalse(1L)).willReturn(false);

        boolean result = notificationService.hasUnreadNotification(user);

        assertThat(result).isFalse();
        verify(notificationRepository).existsByUserIdAndReadFalse(1L);
    }
}
