package com.chungbazi.server.global.logging;

import java.util.Map;
import java.util.UUID;
import org.slf4j.MDC;

public final class ScheduledWorkflowMdc implements AutoCloseable {

    public static final String WORKFLOW_KEY = "workflow";
    public static final String WORKFLOW_ID_KEY = "workflowId";

    private final Map<String, String> previousContext;

    private ScheduledWorkflowMdc(String workflow) {
        if (workflow == null || workflow.isBlank()) {
            throw new IllegalArgumentException("workflow must not be blank");
        }

        previousContext = MDC.getCopyOfContextMap();
        MDC.put(WORKFLOW_KEY, workflow);
        MDC.put(WORKFLOW_ID_KEY, UUID.randomUUID().toString());
    }

    public static ScheduledWorkflowMdc start(String workflow) {
        return new ScheduledWorkflowMdc(workflow);
    }

    @Override
    public void close() {
        MdcContext.replace(previousContext);
    }
}
