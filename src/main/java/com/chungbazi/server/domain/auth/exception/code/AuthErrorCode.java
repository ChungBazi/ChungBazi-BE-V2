package com.chungbazi.server.domain.auth.exception.code;

import com.chungbazi.server.global.common.code.BaseErrorCode;
import com.chungbazi.server.global.common.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401_1", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401_2", "만료된 토큰입니다."),
    KAKAO_API_ERROR(HttpStatus.BAD_GATEWAY, "AUTH502_1", "카카오 API 요청에 실패했습니다."),
    KAKAO_REQUIRED_INFO_MISSING(HttpStatus.BAD_REQUEST, "AUTH400_1", "카카오 사용자 필수 정보가 누락되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getReason() {
        return ErrorReasonDto.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDto getReasonHttpStatus() {
        return ErrorReasonDto.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
