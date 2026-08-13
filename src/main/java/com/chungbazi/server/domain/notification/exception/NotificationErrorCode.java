package com.chungbazi.server.domain.notification.exception;

import com.chungbazi.server.global.common.code.BaseErrorCode;
import com.chungbazi.server.global.common.code.ErrorReasonDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements BaseErrorCode {
    CHILD_NOTIFICATION_ENABLED_WHILE_ALL_DISABLED(HttpStatus.BAD_REQUEST, "NOTIFICATION4001", "전체 알림이 꺼져 있으면 내 정책 알림과 청바지 알림을 켤 수 없습니다."),
    ALL_NOTIFICATION_ENABLED_WITHOUT_CHILD(HttpStatus.BAD_REQUEST, "NOTIFICATION4002", "전체 알림이 켜져 있으면 내 정책 알림과 청바지 알림 중 하나 이상을 켜야 합니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION4041", "알림을 찾을 수 없습니다."),
    NOTIFICATION_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION4042", "알림 설정을 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getReason() {
        return ErrorReasonDto.builder()
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public ErrorReasonDto getReasonHttpStatus() {
        return ErrorReasonDto.builder()
                .isSuccess(false)
                .code(code)
                .message(message)
                .httpStatus(httpStatus)
                .build();
    }
}
