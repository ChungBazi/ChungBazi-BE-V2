package com.chungbazi.server.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    static final String REQUEST_ID_MDC_KEY = "requestId";

    private static final long SLOW_REQUEST_THRESHOLD_MS = 2_000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();
        String previousRequestId = MDC.get(REQUEST_ID_MDC_KEY);
        long startedAt = System.nanoTime();
        boolean failedBeforeResponse = false;

        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException exception) {
            failedBeforeResponse = true;
            throw exception;
        } finally {
            int status = failedBeforeResponse
                    ? HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                    : response.getStatus();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

            writeAccessLog(request.getMethod(), request.getRequestURI(), status, durationMs);
            restorePreviousRequestId(previousRequestId);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = pathWithinApplication(request);
        return path.startsWith("/actuator/")
                || path.equals("/actuator")
                || path.startsWith("/swagger-ui/")
                || path.equals("/swagger-ui")
                || path.startsWith("/v3/api-docs/")
                || path.equals("/v3/api-docs")
                || path.equals("/v1/global/health-check");
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }

    private void writeAccessLog(
            String method,
            String uri,
            int status,
            long durationMs
    ) {
        LoggingEventBuilder event = loggingEventFor(status, durationMs);
        event.addKeyValue("method", method)
                .addKeyValue("uri", uri)
                .addKeyValue("status", status)
                .addKeyValue("durationMs", durationMs)
                .log("HTTP request completed");
    }

    private LoggingEventBuilder loggingEventFor(int status, long durationMs) {
        if (status >= HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            return log.atError();
        }
        if (durationMs >= SLOW_REQUEST_THRESHOLD_MS) {
            return log.atWarn();
        }
        return log.atInfo();
    }

    private String pathWithinApplication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (!contextPath.isEmpty() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }
        return uri;
    }

    private void restorePreviousRequestId(String previousRequestId) {
        if (previousRequestId == null) {
            MDC.remove(REQUEST_ID_MDC_KEY);
            return;
        }
        MDC.put(REQUEST_ID_MDC_KEY, previousRequestId);
    }
}
