package com.chungbazi.server.domain.notification.api.dto.response;

import com.chungbazi.server.domain.notification.domain.NotificationSetting;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.type.SocialType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationSettingResponseTest {

    @Test
    void mapsEveryNotificationSettingState() {
        User user = User.create(
                "provider-id",
                SocialType.KAKAO,
                "user@example.com",
                "사용자",
                null
        );
        NotificationSetting setting = NotificationSetting.create(user);
        setting.updateNotificationSetting(true, false, true);

        NotificationSettingResponse response = NotificationSettingResponse.from(setting);

        assertThat(response.allNotificationEnabled()).isTrue();
        assertThat(response.policyNotificationEnabled()).isFalse();
        assertThat(response.chungbaziNotificationEnabled()).isTrue();
    }
}
