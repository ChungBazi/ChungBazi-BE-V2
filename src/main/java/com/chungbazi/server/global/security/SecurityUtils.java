package com.chungbazi.server.global.security;

import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.code.exception.GeneralException;
import com.chungbazi.server.global.common.code.status.ErrorStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken
        ) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getUser();
    }
}
