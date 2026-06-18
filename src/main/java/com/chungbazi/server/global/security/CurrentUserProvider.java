package com.chungbazi.server.global.security;

import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.exception.UserErrorCode;
import com.chungbazi.server.domain.user.exception.UserException;
import com.chungbazi.server.domain.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUser().getId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }
}
