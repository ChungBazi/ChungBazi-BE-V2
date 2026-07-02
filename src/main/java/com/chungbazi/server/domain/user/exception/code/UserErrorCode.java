package com.chungbazi.server.domain.user.exception.code;

import com.chungbazi.server.global.common.code.BaseErrorCode;
import com.chungbazi.server.global.common.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404_1", "사용자를 찾을 수 없습니다."),
    INVALID_USER_NAME(HttpStatus.BAD_REQUEST, "USER400_1", "사용자 이름을 입력해주세요."),
    INVALID_BIRTH(HttpStatus.BAD_REQUEST, "USER400_2", "생년월일을 선택해주세요."),
    INVALID_SIDO_CODE(HttpStatus.BAD_REQUEST, "USER400_3", "시/도를 선택해주세요."),
    INVALID_SIGUNGU_CODE(HttpStatus.BAD_REQUEST, "USER400_4", "시/군/구를 선택해주세요."),
    INVALID_EDUCATION_CODE(HttpStatus.BAD_REQUEST, "USER400_5", "학력을 선택해주세요."),
    INVALID_EMPLOYMENT_CODE(HttpStatus.BAD_REQUEST, "USER400_6", "취업 상태를 선택해주세요."),
    INVALID_INCOME_LEVEL(HttpStatus.BAD_REQUEST, "USER400_7", "소득 구간을 선택해주세요."),
    INVALID_INTEREST_CATEGORY_COUNT(HttpStatus.BAD_REQUEST, "USER400_8", "관심 분야는 3개 이상 선택해야 합니다."),
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
                .build();
    }
}
