package com.chungbazi.server.domain.auth.api.docs;

import com.chungbazi.server.domain.auth.api.dto.request.AppleLoginRequest;
import com.chungbazi.server.domain.auth.api.dto.request.AuthReissueRequest;
import com.chungbazi.server.domain.auth.api.dto.request.KakaoLoginRequest;
import com.chungbazi.server.domain.auth.api.dto.response.AuthReissueResponse;
import com.chungbazi.server.domain.auth.api.dto.response.AuthTokenResponse;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "[Auth]", description = "인증/인가 관련 API")
public interface AuthDocs {
    @Operation(
            summary = "카카오 로그인 API",
            description = """
                    ### RequestBody
                    ---
                    - `accessToken`: iOS Kakao SDK 로그인 성공 후 발급받은 카카오 accessToken
                    - `fcmToken`: 현재 로그인한 기기의 fcmToken
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "카카오 로그인이 성공적으로 실행됐습니다."
            )
    })
    CommonResponse<AuthTokenResponse> loginWithKakao(
            @Valid @RequestBody KakaoLoginRequest request
    );

    @Operation(
            summary = "애플 로그인 API",
            description = """
                     ### RequestBody
                     ---
                     - `idToken`: iOS Apple 로그인 성공 후 발급받은 identityToken
                     - `name`: Apple에서 전달받은 사용자 이름, 최초 로그인 이후에는 null일 수 있음
                     - `fcmToken`: 현재 로그인한 기기의 fcmToken
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "애플 로그인이 성공적으로 실행됐습니다."
            )
    })
    CommonResponse<AuthTokenResponse> loginWithApple(
            @Valid @RequestBody AppleLoginRequest request
    );

    @Operation(
            summary = "token 재발급 API",
            description = """
                    ### RequestBody
                    ---
                    `refreshToken`: 유효한 refreshToken
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "token 재발급이 성공적으로 실행되었습니다."
            )
    })
    CommonResponse<AuthReissueResponse> reissueToken(
            @Valid @RequestBody AuthReissueRequest request
    );

    @Operation(
            summary = "사용자 로그아웃 API",
            description = "사용자 로그아웃을 진행합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 로그아웃이 성공적으로 실행되었습니다."
            )
    })
    CommonResponse<String> logout(
            @CurrentUser User user,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    );
}
