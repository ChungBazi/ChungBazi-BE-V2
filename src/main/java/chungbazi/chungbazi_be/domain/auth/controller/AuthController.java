package chungbazi.chungbazi_be.domain.auth.controller;

import chungbazi.chungbazi_be.domain.auth.dto.ResetPasswordNoAuthRequestDTO;
import chungbazi.chungbazi_be.domain.auth.dto.TokenDTO;
import chungbazi.chungbazi_be.domain.auth.dto.TokenRequestDTO;
import chungbazi.chungbazi_be.domain.auth.dto.TokenResponseDTO;
import chungbazi.chungbazi_be.domain.auth.service.AuthService;
import chungbazi.chungbazi_be.domain.user.entity.enums.OAuthProvider;
import chungbazi.chungbazi_be.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    // 일반 사용자 회원가입 API
    @PostMapping("/register")
    @Operation(summary = "일반 사용자 회원가입 API", description = "사용자 정보를 받아서 DB에 저장하고 JWT 토큰을 생성")
    public ApiResponse<String> registerUser(@Valid @RequestBody TokenRequestDTO.SignUpTokenRequestDTO request) {
        authService.registerUser(request);
        return ApiResponse.onSuccess("Signup successful.");
    }

    // 일반 사용자 닉네임 등록 API
    @PostMapping("/register-nickname")
    @Operation(summary = "일반 사용자 닉네임 등록 API", description = "사용자 닉네임 등록")
    public ApiResponse<String> registerNickName(@Valid @RequestBody TokenRequestDTO.NickNameRequestDTO request) {
        authService.registerNickName(request);
        return ApiResponse.onSuccess("register nickName successful.");
    }

    // 일반 사용자 로그인 API
    @PostMapping("/login")
    @Operation(summary = "일반 사용자 로그인 API", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 생성")
    public ApiResponse<TokenResponseDTO.LoginTokenResponseDTO> loginUser(@Valid @RequestBody TokenRequestDTO.LoginTokenRequestDTO request) {
        TokenDTO token = authService.loginUser(request);
        return ApiResponse.onSuccess(authService.createLoginTokenResponse(token));
    }

    @PostMapping("/kakao-login")
    @Operation(summary = "사용자 등록 및 JWT 생성 API", description = "사용자 정보를 받아서 DB에 저장하고 JWT 토큰을 생성")
    public ApiResponse<TokenResponseDTO.LoginTokenResponseDTO> kakaoLoginUser(@Valid @RequestBody TokenRequestDTO.KakaoLoginTokenRequestDTO request) {
        TokenDTO token = authService.kakaolLoginUser(request,OAuthProvider.KAKAO);
        return ApiResponse.onSuccess(authService.createLoginTokenResponse(token));
    }

    @PostMapping("/apple-login")
    @Operation(summary = "Apple 로그인 API", description = "Apple idToken과 fcmToken으로 사용자 인증 후 JWT 토큰을 생성")
    public ApiResponse<TokenResponseDTO.LoginTokenResponseDTO> appleLoginUser(@Valid @RequestBody TokenRequestDTO.AppleLoginTokenRequestDTO request) {
        TokenDTO token = authService.appleLoginUser(request,OAuthProvider.APPLE);
        return ApiResponse.onSuccess(authService.createLoginTokenResponse(token));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "JWT 토큰 갱신 API", description = "refresh Token 으로 새로운 access Token 발급")
    public ApiResponse<TokenResponseDTO.RefreshTokenResponseDTO> refreshAccessToken(@Valid @RequestBody TokenRequestDTO.refreshTokenRequestDTO request) {
        TokenDTO newToken = authService.recreateAccessToken(request.getRefreshToken());
        return ApiResponse.onSuccess(authService.createRefreshTokenResponse(newToken));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API", description = "refresh Token 삭제하고 access Token 블랙리스트에 추가")
    public ApiResponse<String> logout() {
        authService.logoutUser();
        return ApiResponse.onSuccess("Logout successful.");
    }

    @DeleteMapping("/delete-account")
    @Operation(summary = "회원 탈퇴 API", description = "access Token을 블랙리스트에 추가하고 refresh Token 삭제, 회원 정보 삭제")
    public ApiResponse<String> deleteAccount() {
        authService.deleteUserAccount();
        return ApiResponse.onSuccess("Account deletion successful.");
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody @Valid TokenRequestDTO.ResetPasswordRequestDTO request) {
        authService.resetPassword(request.getNewPassword());
        return ApiResponse.onSuccess("비밀번호가 성공적으로 변경되었습니다.");
    }

    @PostMapping("/reset-password/no-auth")
    @Operation(summary = "비밀번호 재설정 API(로그인X)", description = "이메일 + 인증번호 입력 후 비밀번호를 재설정한다.")
    public ApiResponse<String> resetPasswordNoAuth(@RequestBody @Valid ResetPasswordNoAuthRequestDTO request) {
        authService.resetPasswordWithEmailAndCode(request);
        return ApiResponse.onSuccess("비밀번호가 성공적으로 변경되었습니다. 다시 로그인해주세요.");
    }
}