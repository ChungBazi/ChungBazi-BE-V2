package com.chungbazi.server.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chungbazi.server.domain.auth.application.TokenBlacklist;
import com.chungbazi.server.domain.auth.exception.AuthException;
import com.chungbazi.server.domain.auth.exception.code.AuthErrorCode;
import com.chungbazi.server.global.security.CustomUserDetailService;
import com.chungbazi.server.global.security.handler.JwtAuthenticationFailureHandler;
import com.chungbazi.server.global.security.handler.TokenBlacklistHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;

import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT 인증 필터")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private CustomUserDetailService customUserDetailService;

    @Mock
    private TokenBlacklist tokenBlacklist;

    @Mock
    private TokenBlacklistHandler tokenBlacklistHandler;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        JwtAuthenticationFailureHandler failureHandler =
                new JwtAuthenticationFailureHandler(new ObjectMapper());
        filter = new JwtAuthenticationFilter(
                jwtProvider,
                customUserDetailService,
                tokenBlacklist,
                tokenBlacklistHandler,
                failureHandler
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("JWT 인증 실패 시 오류 응답을 반환하고 필터 체인을 중단한다")
    @ParameterizedTest(name = "{index}: {3}")
    @MethodSource("authenticationFailures")
    void returnsUnauthorizedWithoutContinuingFilterChain(
            AuthErrorCode errorCode,
            String expectedCode,
            String expectedMessage,
            String scenario
    ) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/user/me");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("existing-user", null)
        );

        when(jwtProvider.validateToken("invalid-token"))
                .thenThrow(new AuthException(errorCode));

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString())
                .contains("\"isSuccess\":false")
                .contains("\"code\":\"" + expectedCode + "\"")
                .contains("\"message\":\"" + expectedMessage + "\"");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(any(), any());
    }

    private static Stream<Arguments> authenticationFailures() {
        return Stream.of(
                Arguments.of(
                        AuthErrorCode.INVALID_TOKEN,
                        "AUTH401_1",
                        "유효하지 않은 토큰입니다.",
                        "유효하지 않은 토큰"
                ),
                Arguments.of(
                        AuthErrorCode.EXPIRED_TOKEN,
                        "AUTH401_2",
                        "만료된 토큰입니다.",
                        "만료된 토큰"
                )
        );
    }
}
