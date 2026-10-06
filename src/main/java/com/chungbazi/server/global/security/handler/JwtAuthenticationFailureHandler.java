package com.chungbazi.server.global.security.handler;

import com.chungbazi.server.domain.auth.exception.AuthException;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.common.code.ErrorReasonDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public void handle(HttpServletResponse response, AuthException exception) throws IOException {
        ErrorReasonDto reason = exception.getErrorReasonHttpStatus();

        response.setStatus(reason.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(
                response.getWriter(),
                CommonResponse.onFailure(reason.getCode(), reason.getMessage(), null)
        );
    }
}
