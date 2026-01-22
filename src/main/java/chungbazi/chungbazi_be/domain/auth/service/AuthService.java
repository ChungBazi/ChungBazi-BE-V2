package chungbazi.chungbazi_be.domain.auth.service;

import chungbazi.chungbazi_be.domain.auth.apple.AppleClient;
import chungbazi.chungbazi_be.domain.auth.apple.ApplePublicKeyGenerator;
import chungbazi.chungbazi_be.domain.auth.apple.ApplePublicKeys;
import chungbazi.chungbazi_be.domain.auth.apple.AppleTokenParser;
import chungbazi.chungbazi_be.domain.auth.converter.AuthConverter;
import chungbazi.chungbazi_be.domain.auth.dto.ResetPasswordNoAuthRequestDTO;
import chungbazi.chungbazi_be.domain.auth.dto.TokenDTO;
import chungbazi.chungbazi_be.domain.auth.dto.TokenRequestDTO;
import chungbazi.chungbazi_be.domain.auth.dto.TokenResponseDTO;
import chungbazi.chungbazi_be.domain.auth.jwt.JwtProvider;
import chungbazi.chungbazi_be.domain.auth.jwt.SecurityUtils;
import chungbazi.chungbazi_be.domain.auth.jwt.TokenGenerator;
import chungbazi.chungbazi_be.domain.notification.service.FcmTokenService;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.entity.enums.OAuthProvider;
import chungbazi.chungbazi_be.domain.user.repository.UserRepository;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.BadRequestHandler;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.NotFoundHandler;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.PublicKey;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenGenerator tokenGenerator;
    private final TokenAuthService tokenAuthService;
    private final FcmTokenService fcmTokenService;
    private final AuthConverter authConverter;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppleTokenParser appleTokenParser;
    private final AppleClient appleClient;
    private final ApplePublicKeyGenerator applePublicKeyGenerator;
    private final UserHelper userHelper;
    private final MailService mailService;

    // 일반 회원가입
    public void registerUser(TokenRequestDTO.SignUpTokenRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestHandler(ErrorStatus.ALREADY_EXISTS_EMAIL);
        }
        if (!request.getPassword().equals(request.getCheckPassword())) {
            throw new BadRequestHandler(ErrorStatus.PASSWORD_MISMATCH);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name("닉네임을 등록해주세요.")
                .oAuthProvider(OAuthProvider.LOCAL)
                .build();

        user.createNotificationSetting();

        userRepository.save(user);
    }

    // 일반 회원가입 닉네임 등록
    public void registerNickName(TokenRequestDTO.NickNameRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .map(existingUser -> {
                    if (existingUser.isDeleted()) {
                        throw new BadRequestHandler(ErrorStatus.DEACTIVATED_ACCOUNT);
                    }
                    return existingUser;
                })
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_USER));

        if(userRepository.findByName(request.getName()).isPresent()) {
            throw new BadRequestHandler(ErrorStatus.INVALID_NICKNAME);
        }

        user.updateName(request.getName());
        userRepository.save(user);
    }

    // 일반 로그인
    public TokenDTO loginUser(TokenRequestDTO.LoginTokenRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .map(existingUser -> {
                    if (existingUser.isDeleted()) {
                        throw new BadRequestHandler(ErrorStatus.DEACTIVATED_ACCOUNT);
                    }
                    return existingUser;
                })
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_USER));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestHandler(ErrorStatus.INVALID_CREDENTIALS);
        }
        boolean isFirst = determineIsFirst(user);
        TokenDTO tokenDTO = tokenGenerator.generate(user.getId(), user.getName(), isFirst);
        tokenAuthService.saveRefreshToken(user.getId(), tokenDTO.getRefreshToken(), tokenDTO.getRefreshExp());
        fcmTokenService.saveFcmToken(user.getId(), request.getFcmToken());
        return tokenDTO;
    }

    // 카카오 로그인
    public TokenDTO kakaolLoginUser(TokenRequestDTO.KakaoLoginTokenRequestDTO request, OAuthProvider oAuthProvider) {
        User user = findOrCreateUserForKakaoLogin(request, oAuthProvider);
        boolean isFirst = determineIsFirst(user);
        TokenDTO tokenDTO = tokenGenerator.generate(user.getId(), user.getName(), isFirst);
        tokenAuthService.saveRefreshToken(user.getId(), tokenDTO.getRefreshToken(), tokenDTO.getRefreshExp());
        fcmTokenService.saveFcmToken(user.getId(), request.getFcmToken());
        return tokenDTO;
    }

    public User findOrCreateUserForKakaoLogin(TokenRequestDTO.KakaoLoginTokenRequestDTO request, OAuthProvider oAuthProvider) {
        return userRepository.findByEmail(request.getEmail())
                .map(existingUser -> {
                    if (existingUser.isDeleted()) {
                        throw new BadRequestHandler(ErrorStatus.DEACTIVATED_ACCOUNT);
                    }
                    return existingUser;
                })
                .orElseGet(() -> createUserForKakaoLogin(request, oAuthProvider));
    }


    public User createUserForKakaoLogin(TokenRequestDTO.KakaoLoginTokenRequestDTO request, OAuthProvider oAuthProvider) {
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password("")
                .oAuthProvider(oAuthProvider)
                .build();

        user.createNotificationSetting();

        return userRepository.save(user);
    }

    // 애플 로그인
    public TokenDTO appleLoginUser(TokenRequestDTO.AppleLoginTokenRequestDTO request, OAuthProvider oAuthProvider) {
        // AppleTokenService 를 통해 idToken 검증 + Claims 추출을 하나의 메서드로 캡슐화
        Claims claims = parseAndValidateIdToken(request.getIdToken());

        // 4. appleUserId 와 email 추출
        String appleUserId = claims.getSubject();
        String email = claims.get("email", String.class) != null
                ? claims.get("email", String.class)
                : appleUserId + "@apple.com";

        // 5. 회원 조회 또는 신규 등록
        User user = userRepository.findByEmail(email)
                .map(existingUser -> {
                    if (existingUser.isDeleted()) {
                        throw new BadRequestHandler(ErrorStatus.DEACTIVATED_ACCOUNT);
                    }
                    return existingUser;
                })
                .orElseGet(() -> createUserForAppleLogin(email, appleUserId, oAuthProvider));

        // 6. FCM 토큰 저장
        fcmTokenService.saveFcmToken(user.getId(), request.getFcmToken());

        // 7. JWT 토큰 발급 및 저장
        boolean isFirst = determineIsFirst(user);
        TokenDTO tokenDTO = tokenGenerator.generate(user.getId(), user.getName(), isFirst);
        tokenAuthService.saveRefreshToken(user.getId(), tokenDTO.getRefreshToken(), tokenDTO.getRefreshExp());
        fcmTokenService.saveFcmToken(user.getId(), request.getFcmToken());

        return tokenDTO;
    }


    public Claims parseAndValidateIdToken(String idToken) {
        var header = appleTokenParser.parseHeader(idToken);

        // 공개키 가져오기 및 변환
        ApplePublicKeys applePublicKeys = appleClient.getApplePublicKeys();
        PublicKey publicKey = applePublicKeyGenerator.generate(header, applePublicKeys);

        // idToken 검증 후 Claims 추출
        return appleTokenParser.extractClaims(idToken, publicKey);
    }

    public User createUserForAppleLogin(String email, String appleUserId, OAuthProvider oAuthProvider) {
        User user = User.builder()
                .email(email)
                .name(appleUserId)
                .password("")
                .oAuthProvider(oAuthProvider)
                .build();

        user.createNotificationSetting();

        return userRepository.save(user);
    }


    // JWT 토큰 관련 처리
    public TokenDTO recreateAccessToken(String refreshToken) {
        Long userId = jwtProvider.getUserIdParsingFromToken(refreshToken);
        tokenAuthService.validateRefreshToken(userId, refreshToken);

        User user = getUserById(userId);

        TokenDTO newToken = tokenGenerator.generate(userId, user.getName(), false);

        tokenAuthService.saveRefreshToken(userId, newToken.getRefreshToken(), newToken.getRefreshExp());

        tokenAuthService.addToBlackList(refreshToken, "expired", newToken.getRefreshExp());

        return newToken;
    }

    // 로그아웃
    public void logoutUser(String token) {
        tokenAuthService.validateNotBlackListed(token);
        Long userId = SecurityUtils.getUserId();
        tokenAuthService.addToBlackList(token, "logout", 3600L);
        tokenAuthService.deleteRefreshToken(userId);
    }

    // 회원 탈퇴
    public void deleteUserAccount(String token) {
        tokenAuthService.validateNotBlackListed(token);
        Long userId = SecurityUtils.getUserId();
        tokenAuthService.addToBlackList(token, "delete-account", 3600L);
        tokenAuthService.deleteRefreshToken(userId);
        deleteUser(userId);
        fcmTokenService.deleteToken(userId);
    }

    // 응답
    public TokenResponseDTO.LoginTokenResponseDTO createLoginTokenResponse(TokenDTO token) {
        return authConverter.toLoginTokenResponse(token);
    }

    public TokenResponseDTO.RefreshTokenResponseDTO createRefreshTokenResponse(TokenDTO token) {
        return authConverter.toRefreshTokenResponse(token);
    }


    public boolean determineIsFirst(User user) {
        return !user.isSurveyStatus();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_USER));
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_USER));
        // 유저 익명화 (username & email 무력화)
        userRepository.anonymizeUser(userId);
    }

    // 비밀번호 재설정
    public void resetPassword(String newPassword) {
        User user = userHelper.getAuthenticatedUser();

        // 새 비밀번호가 기존 비밀번호와 같은지 확인
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BadRequestHandler(ErrorStatus.SAME_AS_OLD_PASSWORD);
        }
        // 새 비밀번호 인코딩 및 저장
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);
        userRepository.save(user);
    }

    public void resetPasswordWithEmailAndCode(ResetPasswordNoAuthRequestDTO request) {
        mailService.verifiedCode(request.getEmail(),request.getAuthCode());

        // 2) 사용자 존재 여부
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_USER));

        // 3) 새 비밀번호가 기존과 동일한지 검사
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestHandler(ErrorStatus.SAME_AS_OLD_PASSWORD);
        }

        // 4) 비밀번호 업데이트
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

    }
}