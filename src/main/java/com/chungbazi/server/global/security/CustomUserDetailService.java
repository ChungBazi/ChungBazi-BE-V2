package com.chungbazi.server.global.security;

import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.exception.UserException;
import com.chungbazi.server.domain.user.exception.code.UserErrorCode;
import com.chungbazi.server.domain.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        return new CustomUserDetails(user);
    }
}
