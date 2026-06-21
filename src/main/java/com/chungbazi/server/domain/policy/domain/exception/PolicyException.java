package com.chungbazi.server.domain.policy.domain.exception;

import com.chungbazi.server.global.common.code.exception.GeneralException;

public class PolicyException extends GeneralException {

    public PolicyException(PolicyErrorCode errorCode) {
        super(errorCode);
    }
}
