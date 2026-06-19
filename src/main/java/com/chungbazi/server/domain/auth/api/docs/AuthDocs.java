package com.chungbazi.server.domain.auth.api.docs;

import com.chungbazi.server.domain.auth.api.dto.request.KakaoLoginRequest;
import com.chungbazi.server.domain.auth.api.dto.response.AuthTokenResponse;
import com.chungbazi.server.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

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
}
