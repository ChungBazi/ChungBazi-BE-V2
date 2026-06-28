package com.chungbazi.server.domain.user.api;

import com.chungbazi.server.domain.user.api.dto.UserOnboardingRequest;
import com.chungbazi.server.domain.user.application.UserService;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/onboarding")
    public CommonResponse<String> saveUserOnboarding(
            @CurrentUser User user,
            @Valid @RequestBody UserOnboardingRequest request
    ) {
        userService.saveUserOnboarding(user, request);
        return CommonResponse.onSuccess("온보딩이 성공적으로 완료되었습니다.");
    }
}
