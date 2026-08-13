package com.chungbazi.server.domain.notification.application.validator;

import com.chungbazi.server.domain.notification.api.dto.request.NotificationSettingUpdateRequest;
import com.chungbazi.server.domain.notification.exception.NotificationErrorCode;
import com.chungbazi.server.domain.notification.exception.NotificationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationSettingValidatorTest {

    private final NotificationSettingValidator validator = new NotificationSettingValidator();

    @Test
    void rejectsEnabledChildWhenAllNotificationIsDisabled() {
        NotificationSettingUpdateRequest request = request(false, true, false);

        assertError(request, NotificationErrorCode.CHILD_NOTIFICATION_ENABLED_WHILE_ALL_DISABLED);
    }

    @Test
    void rejectsBothEnabledChildrenWhenAllNotificationIsDisabled() {
        NotificationSettingUpdateRequest request = request(false, true, true);

        assertError(request, NotificationErrorCode.CHILD_NOTIFICATION_ENABLED_WHILE_ALL_DISABLED);
    }

    @Test
    void rejectsDisabledChildrenWhenAllNotificationIsEnabled() {
        NotificationSettingUpdateRequest request = request(true, false, false);

        assertError(request, NotificationErrorCode.ALL_NOTIFICATION_ENABLED_WITHOUT_CHILD);
    }

    @Test
    void acceptsValidCombinations() {
        assertThatCode(() -> validator.validate(request(false, false, false)))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validate(request(true, true, false)))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validate(request(true, false, true)))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validate(request(true, true, true)))
                .doesNotThrowAnyException();
    }

    private void assertError(
            NotificationSettingUpdateRequest request,
            NotificationErrorCode errorCode
    ) {
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(NotificationException.class)
                .extracting(exception -> ((NotificationException) exception).getCode())
                .isEqualTo(errorCode);
    }

    private NotificationSettingUpdateRequest request(
            boolean allEnabled,
            boolean policyEnabled,
            boolean chungbaziEnabled
    ) {
        return new NotificationSettingUpdateRequest(
                allEnabled,
                policyEnabled,
                chungbaziEnabled
        );
    }
}
