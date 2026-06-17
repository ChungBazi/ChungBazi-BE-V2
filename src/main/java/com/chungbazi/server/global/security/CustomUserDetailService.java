package com.chungbazi.server.global.security;

import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.infrastructure.UserRepository;
import com.chungbazi.server.global.common.code.exception.GeneralException;
import com.chungbazi.server.global.common.code.status.ErrorStatus;
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
                .orElseThrow(() -> new GeneralException(ErrorStatus._UNAUTHORIZED)); // TODO: 에러 코드 수정

        return new CustomUserDetails(user);
    }
}
