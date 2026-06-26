package com.chungbazi.server.global.security.jwt;

import com.chungbazi.server.domain.auth.application.TokenBlacklist;
import com.chungbazi.server.global.security.BearerTokenExtractor;
import com.chungbazi.server.global.security.CustomUserDetailService;
import com.chungbazi.server.global.security.handler.TokenBlacklistHandler;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailService customUserDetailService;
    private final TokenBlacklist tokenBlacklist;
    private final TokenBlacklistHandler tokenBlacklistHandler;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = BearerTokenExtractor.extractOrNull(request.getHeader(HttpHeaders.AUTHORIZATION));

        if (token != null && jwtProvider.validateToken(token)) {
            if (tokenBlacklist.contains(token)) {
                tokenBlacklistHandler.handleBlacklistedToken(response);
                return;
            }
            Claims claims = jwtProvider.getClaims(token);
            String userId = claims.getSubject();

            UserDetails userDetails = customUserDetailService.loadUserByUsername(userId);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
