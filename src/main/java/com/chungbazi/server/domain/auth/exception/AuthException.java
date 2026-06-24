package com.chungbazi.server.domain.auth.exception;

import com.chungbazi.server.global.common.code.BaseErrorCode;
import com.chungbazi.server.global.common.code.exception.GeneralException;

public class AuthException extends GeneralException {
    public AuthException(BaseErrorCode baseErrorCode) {
        super(baseErrorCode);
    }
}
