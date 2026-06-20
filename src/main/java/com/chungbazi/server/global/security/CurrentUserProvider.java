package com.chungbazi.server.global.security;

import com.chungbazi.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    public User getCurrentUser() {
        return SecurityUtils.getCurrentUser();
    }
}
