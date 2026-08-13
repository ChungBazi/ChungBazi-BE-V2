package com.chungbazi.server.domain.notification.application;

import com.chungbazi.server.domain.notification.api.dto.request.NotificationSettingUpdateRequest;
import com.chungbazi.server.domain.notification.api.dto.response.NotificationSettingResponse;
import com.chungbazi.server.domain.notification.application.validator.NotificationSettingValidator;
import com.chungbazi.server.domain.notification.domain.NotificationSetting;
import com.chungbazi.server.domain.notification.domain.repository.NotificationSettingRepository;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.type.SocialType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationSettingServiceTest {

    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    @Mock
    private NotificationSettingValidator notificationSettingValidator;

    @InjectMocks
    private NotificationSettingService notificationSettingService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.create(
                "provider-id",
                SocialType.KAKAO,
                "user@example.com",
                "사용자",
                null
        );
    }

    @Test
    void createsDefaultSettingWhenUserHasNoSetting() {
        given(notificationSettingRepository.findByUser(user)).willReturn(Optional.empty());
        given(notificationSettingRepository.save(any(NotificationSetting.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        NotificationSettingResponse response = notificationSettingService.getNotificationSetting(user);

        assertThat(response.allNotificationEnabled()).isTrue();
        assertThat(response.policyNotificationEnabled()).isTrue();
        assertThat(response.chungbaziNotificationEnabled()).isTrue();
        verify(notificationSettingRepository).save(any(NotificationSetting.class));
    }

    @Test
    void updatesAllSettingsAtOnce() {
        NotificationSetting setting = NotificationSetting.create(user);
        given(notificationSettingRepository.findByUser(user)).willReturn(Optional.of(setting));
        NotificationSettingUpdateRequest request = new NotificationSettingUpdateRequest(
                true,
                false,
                true
        );

        NotificationSettingResponse response =
                notificationSettingService.updateNotificationSetting(user, request);

        assertThat(response.allNotificationEnabled()).isTrue();
        assertThat(response.policyNotificationEnabled()).isFalse();
        assertThat(response.chungbaziNotificationEnabled()).isTrue();
        verify(notificationSettingValidator).validate(request);
    }
}
