package chungbazi.chungbazi_be.domain.auth.exception;

import chungbazi.chungbazi_be.domain.auth.jwt.JwtProvider;
import chungbazi.chungbazi_be.global.apiPayload.exception.GeneralException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {


        try {
            filterChain.doFilter(request, response);
        } catch (GeneralException e) {
            setErrorResponse(response, e.getErrorReasonHttpStatus().getCode(), e.getErrorReasonHttpStatus().getMessage());
        }
    }

    private void setErrorResponse(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // `ApiResponse.onFailure()`의 로직을 그대로 적용
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(Map.of(
                "isSuccess", false,
                "code", code,
                "message", message
        ));

        response.getWriter().write(json);
    }
}
