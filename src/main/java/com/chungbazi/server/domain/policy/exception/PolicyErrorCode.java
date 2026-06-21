package com.chungbazi.server.domain.policy.exception;

import com.chungbazi.server.global.common.code.BaseErrorCode;
import com.chungbazi.server.global.common.code.ErrorReasonDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PolicyErrorCode implements BaseErrorCode {
    INVALID_SIDO_CODE(HttpStatus.BAD_REQUEST, "POLICY4001", "지원하지 않는 시도 코드입니다."),
    REGION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "POLICY4002", "시도 코드와 시군구 코드가 일치하지 않습니다."),
    INVALID_POLICY_CATEGORY(HttpStatus.BAD_REQUEST, "POLICY4003", "유효하지 않은 정책 카테고리입니다."),
    INVALID_POLICY_REGION(HttpStatus.BAD_REQUEST, "POLICY4004", "유효하지 않은 정책 지역입니다."),
    INVALID_POLICY_CURSOR(HttpStatus.BAD_REQUEST, "POLICY4005", "유효하지 않은 정책 조회 커서입니다."),
    REGION_NOT_INITIALIZED(HttpStatus.INTERNAL_SERVER_ERROR, "POLICY5001", "시군구 코드 데이터가 초기화되지 않았습니다.")

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
