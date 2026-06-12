package com.chungbazi.server.global.common.code.exception.handler;

import com.chungbazi.server.global.common.code.BaseErrorCode;
import com.chungbazi.server.global.common.code.exception.GeneralException;

public class UnAuthorizedHandler extends GeneralException {
    public UnAuthorizedHandler(BaseErrorCode baseErrorCode) {super(baseErrorCode);}
}
