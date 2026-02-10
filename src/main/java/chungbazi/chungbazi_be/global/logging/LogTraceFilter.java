package chungbazi.chungbazi_be.global.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LogTraceFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            // 랜덤한 uuid 값을 생성하여 MDC 저장소에 request_id를 키, uuid를 value로 저장
            UUID uuid = UUID.randomUUID();
            MDC.put("traceId", uuid.toString().substring(0, 8));

            String entryPoint = httpRequest.getHeader("X-Entry-Point");
            if (entryPoint == null) entryPoint = "DIRECT";
            MDC.put("entry_point", entryPoint);

            chain.doFilter(request, response);
        } finally {

            MDC.clear();
        }
    }

    @Override
    public void destroy() {
    }
}
