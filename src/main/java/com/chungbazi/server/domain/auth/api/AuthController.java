package com.chungbazi.server.domain.auth.api;

import com.chungbazi.server.domain.auth.api.docs.AuthDocs;
import com.chungbazi.server.domain.auth.api.dto.request.AppleLoginRequest;
import com.chungbazi.server.domain.auth.api.dto.request.AuthReissueRequest;
import com.chungbazi.server.domain.auth.api.dto.request.KakaoLoginRequest;
import com.chungbazi.server.domain.auth.api.dto.response.AuthReissueResponse;
import com.chungbazi.server.domain.auth.api.dto.response.AuthTokenResponse;
import com.chungbazi.server.domain.auth.application.AuthService;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import com.chungbazi.server.global.security.BearerTokenExtractor;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

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

    @Override
    @PostMapping("/apple")
    public CommonResponse<AuthTokenResponse> loginWithApple(@Valid @RequestBody AppleLoginRequest request) {
        return CommonResponse.onSuccess(authService.loginWithApple(request));
    }

    @PostMapping("/reissue")
    public CommonResponse<AuthReissueResponse> reissueToken(@Valid @RequestBody AuthReissueRequest request) {
        return CommonResponse.onSuccess(authService.reissueToken(request));
    }

    @PostMapping("/logout")
    public CommonResponse<String> logout(
            @CurrentUser User user,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        authService.logout(user.getId(), BearerTokenExtractor.extract(authorization));
        return CommonResponse.onSuccess("로그아웃이 성공적으로 실행되었습니다.");
    }
}
