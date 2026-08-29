package com.chungbazi.server.global.security.handler;

import com.chungbazi.server.domain.auth.exception.code.AuthErrorCode;
import com.chungbazi.server.global.common.CommonResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TokenBlacklistHandler {

    private final ObjectMapper objectMapper;

    public void handleBlacklistedToken(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        CommonResponse<Void> errorResponse = CommonResponse.onFailure(AuthErrorCode.LOGGED_OUT_TOKEN);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
