package com.chungbazi.server.global.common.code.exception.handler;

import com.chungbazi.server.global.common.code.BaseErrorCode;
import com.chungbazi.server.global.common.code.exception.GeneralException;

public class NotFoundHandler extends GeneralException {
    public NotFoundHandler(BaseErrorCode baseErrorCode) {super(baseErrorCode);}
}
