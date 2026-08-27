package com.chungbazi.server.global.logging;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

@Component
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable task) {
        Map<String, String> callerContext = MDC.getCopyOfContextMap();

        return () -> {
            Map<String, String> workerContext = MDC.getCopyOfContextMap();
            try {
                MdcContext.replace(callerContext);
                task.run();
            } finally {
                MdcContext.replace(workerContext);
            }
        };
    }
}
