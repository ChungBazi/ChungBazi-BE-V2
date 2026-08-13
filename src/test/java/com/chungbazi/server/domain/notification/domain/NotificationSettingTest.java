package com.chungbazi.server.domain.notification.domain;

import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.type.SocialType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationSettingTest {

    @Test
    void childSettingsCanDifferWhenAllNotificationIsEnabled() {
        NotificationSetting setting = NotificationSetting.create(createUser());

        setting.updateNotificationSetting(true, true, false);

        assertThat(setting.isAllNotificationEnabled()).isTrue();
        assertThat(setting.isPolicyNotificationEnabled()).isTrue();
        assertThat(setting.isChungbaziNotificationEnabled()).isFalse();
    }

    @Test
    void disablingAllNotificationDisablesBothChildSettings() {
        NotificationSetting setting = NotificationSetting.create(createUser());

        setting.updateNotificationSetting(false, true, true);

        assertThat(setting.isAllNotificationEnabled()).isFalse();
        assertThat(setting.isPolicyNotificationEnabled()).isFalse();
        assertThat(setting.isChungbaziNotificationEnabled()).isFalse();
    }

    private User createUser() {
        return User.create(
                "provider-id",
                SocialType.KAKAO,
                "user@example.com",
                "사용자",
                null
        );
    }
}
