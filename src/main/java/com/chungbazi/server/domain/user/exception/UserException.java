package com.chungbazi.server.domain.user.exception;

import com.chungbazi.server.global.common.code.BaseErrorCode;
import com.chungbazi.server.global.common.code.exception.GeneralException;

public class UserException extends GeneralException {

    public UserException(BaseErrorCode baseErrorCode) {
        super(baseErrorCode);
    }
}
