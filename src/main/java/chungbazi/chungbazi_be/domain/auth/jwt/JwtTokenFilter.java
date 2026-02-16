package chungbazi.chungbazi_be.domain.auth.jwt;

import chungbazi.chungbazi_be.domain.auth.exception.TokenBlacklistHandler;
import chungbazi.chungbazi_be.domain.auth.service.TokenAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenAuthService tokenAuthService;
    private final TokenBlacklistHandler tokenBlacklistHandler;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            jwtProvider.validateToken(token);

            // 로그아웃된 accessToken으로 로그인 시도 시, 예외 처리
            if (tokenAuthService.isBlackListed(token)) {
                SecurityContextHolder.clearContext();
                tokenBlacklistHandler.handleBlacklistedToken(response);
                return;
            }
            String userId = jwtProvider.extractSubject(token);

            JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(
                    userId, token, Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
