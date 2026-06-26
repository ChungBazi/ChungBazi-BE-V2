package com.chungbazi.server.global.security;

import com.chungbazi.server.domain.auth.exception.AuthException;
import com.chungbazi.server.domain.auth.exception.code.AuthErrorCode;

public final class BearerTokenExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    public static String extract(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
        return authorization.substring(BEARER_PREFIX.length());
    }

    public static String extractOrNull(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length());
    }
}
