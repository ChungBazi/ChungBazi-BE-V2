package com.chungbazi.server.global.common;

import com.chungbazi.server.global.common.code.BaseCode;
import com.chungbazi.server.global.common.code.BaseErrorCode;
import com.chungbazi.server.global.common.code.status.SuccessStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess","code","message","result"})
public class CommonResponse<T> {
    @JsonProperty("isSuccess")
    private final Boolean isSuccess;
    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    //성공한 경우 응답 생성
    public static <T> CommonResponse<T> onSuccess(T result){
        return new CommonResponse<>(true, SuccessStatus._OK.getCode(), SuccessStatus._OK.getMessage(), result);
    }

    public static <T> CommonResponse<T> of(BaseCode code, T result){
        return new CommonResponse<>(true, code.getReasonHttpStatus().getCode(), code.getReasonHttpStatus().getMessage(),result);
    }

    //실패한 경우 응답 생성
    public static <T> CommonResponse<T> onFailure(String code, String message, T data){
        return new CommonResponse<>(false, code, message, data);
    }

    public static <T> CommonResponse<T> onFailure(BaseErrorCode code) {
        return new CommonResponse<>(false, code.getReasonHttpStatus().getCode(), code.getReasonHttpStatus().getMessage(), null);
    }
}
