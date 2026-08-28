package com.chungbazi.server.global.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();
    private final Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    @BeforeEach
    void setUp() {
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        appender.stop();
        MDC.clear();
    }

    @Test
    void logsRequestSummaryAndReturnsRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/policies");
        request.setContextPath("/api");
        request.setQueryString("keyword=should-not-be-logged");
        request.addHeader("Authorization", "Bearer should-not-be-logged");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> requestIdInChain = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            requestIdInChain.set(MDC.get(RequestLoggingFilter.REQUEST_ID_MDC_KEY));
            ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_CREATED);
        });

        assertThat(response.getHeader(RequestLoggingFilter.REQUEST_ID_HEADER))
                .isEqualTo(requestIdInChain.get())
                .matches("[0-9a-f-]{36}");
        assertThat(MDC.get(RequestLoggingFilter.REQUEST_ID_MDC_KEY)).isNull();

        ILoggingEvent event = assertSingleEvent();
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event.getFormattedMessage()).isEqualTo("HTTP request completed");
        assertThat(event.getMDCPropertyMap()).containsEntry("requestId", requestIdInChain.get());
        assertThat(keyValues(event)).containsEntry("method", "GET")
                .containsEntry("uri", "/api/v1/policies")
                .containsEntry("status", HttpServletResponse.SC_CREATED);
        assertThat(event.getFormattedMessage()).doesNotContain("keyword", "should-not-be-logged");
        assertThat(keyValues(event).values()).allSatisfy(value -> assertThat(String.valueOf(value))
                .doesNotContain("keyword", "should-not-be-logged", "Authorization", "Bearer"));
    }

    @Test
    void excludesActuatorAndSwaggerRequests() throws Exception {
        for (String uri : new String[]{
                "/actuator/prometheus",
                "/api/swagger-ui/index.html",
                "/api/v3/api-docs/swagger-config",
                "/api/v1/global/health-check"
        }) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
            request.setContextPath(uri.startsWith("/api/") ? "/api" : "");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, (servletRequest, servletResponse) -> { });

            assertThat(response.getHeader(RequestLoggingFilter.REQUEST_ID_HEADER)).isNull();
        }

        assertThat(appender.list).isEmpty();
    }

    @Test
    void logsErrorAndCleansMdcWhenChainFails() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/policies");
        request.setContextPath("/api");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw new IOException("connection failed");
        })).isInstanceOf(IOException.class);

        assertThat(MDC.get(RequestLoggingFilter.REQUEST_ID_MDC_KEY)).isNull();
        ILoggingEvent event = assertSingleEvent();
        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(keyValues(event))
                .containsEntry("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    private ILoggingEvent assertSingleEvent() {
        assertThat(appender.list).hasSize(1);
        return appender.list.getFirst();
    }

    private Map<String, Object> keyValues(ILoggingEvent event) {
        return event.getKeyValuePairs().stream()
                .collect(java.util.stream.Collectors.toMap(pair -> pair.key, pair -> pair.value));
    }
}
