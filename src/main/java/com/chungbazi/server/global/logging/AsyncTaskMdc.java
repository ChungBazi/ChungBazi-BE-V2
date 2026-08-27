package com.chungbazi.server.global.logging;

import org.slf4j.MDC;

public final class AsyncTaskMdc implements AutoCloseable {

    public static final String TASK_NAME_KEY = "taskName";

    private final String previousTaskName;

    private AsyncTaskMdc(String taskName) {
        if (taskName == null || taskName.isBlank()) {
            throw new IllegalArgumentException("taskName must not be blank");
        }

        previousTaskName = MDC.get(TASK_NAME_KEY);
        MDC.put(TASK_NAME_KEY, taskName);
    }

    public static AsyncTaskMdc start(String taskName) {
        return new AsyncTaskMdc(taskName);
    }

    @Override
    public void close() {
        if (previousTaskName == null) {
            MDC.remove(TASK_NAME_KEY);
            return;
        }
        MDC.put(TASK_NAME_KEY, previousTaskName);
    }
}
