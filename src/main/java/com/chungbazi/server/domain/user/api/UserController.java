package com.chungbazi.server.domain.user.api;

import com.chungbazi.server.domain.user.api.dto.UserNameRequest;
import com.chungbazi.server.domain.user.api.dto.UserOnboardingRequest;
import com.chungbazi.server.domain.user.api.dto.UserPolicyRequest;
import com.chungbazi.server.domain.user.application.UserService;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/name")
    public CommonResponse<String> updateUserName(
            @CurrentUser User user,
            @Valid @RequestBody UserNameRequest request
    ) {
        userService.updateUserName(user, request);
        return CommonResponse.onSuccess("사용자 이름이 성공적으로 수정되었습니다.");
    }

    @PatchMapping("/policy-profile")
    public CommonResponse<String> updateUserPolicy(
            @CurrentUser User user,
            @Valid @RequestBody UserPolicyRequest request
    ) {
        userService.updateUserPolicy(user, request);
        return CommonResponse.onSuccess("사용자 정책 추천 기준이 성공적으로 수정되었습니다.");
    }
}
