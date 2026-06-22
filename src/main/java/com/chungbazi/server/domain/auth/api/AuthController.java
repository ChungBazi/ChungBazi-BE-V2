package com.chungbazi.server.domain.auth.api;

import com.chungbazi.server.domain.auth.api.docs.AuthDocs;
import com.chungbazi.server.domain.auth.api.dto.request.AppleLoginRequest;
import com.chungbazi.server.domain.auth.api.dto.request.KakaoLoginRequest;
import com.chungbazi.server.domain.auth.api.dto.response.AuthTokenResponse;
import com.chungbazi.server.domain.auth.application.AuthService;
import com.chungbazi.server.global.common.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController implements AuthDocs {

    private final AuthService authService;

    @Override
    @PostMapping("/kakao")
    public CommonResponse<AuthTokenResponse> loginWithKakao(@Valid @RequestBody KakaoLoginRequest request) {
        return CommonResponse.onSuccess(authService.loginWithKakao(request));
    }

    @PostMapping("/apple")
    public CommonResponse<AuthTokenResponse> loginWithApple(@Valid @RequestBody AppleLoginRequest request) {
        return CommonResponse.onSuccess(authService.loginWithApple(request));
    }
}
