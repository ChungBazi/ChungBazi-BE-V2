package com.chungbazi.server.global.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SensitiveDataMaskingJsonMembersCustomizerTest {

    @Test
    void masksBearerToken() {
        String log = "Authorization: Bearer eyJhbGciOiJIUzI1Ni.payload.signature";

        String masked = SensitiveDataMaskingJsonMembersCustomizer.mask(log);

        assertThat(masked)
                .isEqualTo("Authorization: ***")
                .doesNotContain("eyJhbGciOiJIUzI1Ni");
    }

    @Test
    void masksCommonSensitiveKeyValues() {
        String log = "password=hello secret:world api-key=external-key fcmToken=device-token "
                + "payload={\"refreshToken\":\"jwt-value\"}";

        String masked = SensitiveDataMaskingJsonMembersCustomizer.mask(log);

        assertThat(masked)
                .isEqualTo("password=*** secret:*** api-key=*** fcmToken=*** "
                        + "payload={\"refreshToken\":***}")
                .doesNotContain("hello", "world", "external-key", "device-token", "jwt-value");
    }

    @Test
    void keepsOrdinaryLogMessage() {
        String log = "정책 동기화 완료. 읽은 정책 수=10";

        assertThat(SensitiveDataMaskingJsonMembersCustomizer.mask(log)).isEqualTo(log);
    }
}
