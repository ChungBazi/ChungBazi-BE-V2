package chungbazi.chungbazi_be.domain.auth.exception;

import chungbazi.chungbazi_be.global.apiPayload.ApiResponse;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TokenBlacklistHandler {

    private final ObjectMapper objectMapper;

    public void handleBlacklistedToken(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Void> failureResponse = ApiResponse.onFailure(
                ErrorStatus.INVALID_TOKEN.getCode(),
                ErrorStatus.INVALID_TOKEN.getMessage(),
                null
        );
        response.getWriter().write(objectMapper.writeValueAsString(failureResponse));
    }
}
