package com.chungbazi.server.global.common.code.status;

import com.chungbazi.server.global.common.code.BaseErrorCode;
import com.chungbazi.server.global.common.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
    //일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    _INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401_1", "유효하지 않은 토큰입니다."),
    _EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401_2", "만료된 토큰입니다."),
    _KAKAO_API_ERROR(HttpStatus.BAD_GATEWAY, "AUTH502_1", "카카오 API 요청에 실패했습니다."),
    _KAKAO_REQUIRED_INFO_MISSING(HttpStatus.BAD_REQUEST, "AUTH400_1", "카카오 사용자 필수 정보가 누락되었습니다."),
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
