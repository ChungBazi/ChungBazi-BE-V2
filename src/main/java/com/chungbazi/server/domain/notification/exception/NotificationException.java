package com.chungbazi.server.domain.notification.exception;

import com.chungbazi.server.global.common.code.exception.GeneralException;

public class NotificationException extends GeneralException {

    public NotificationException(NotificationErrorCode errorCode) {
        super(errorCode);
    }
}
