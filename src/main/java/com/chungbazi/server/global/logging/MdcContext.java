package com.chungbazi.server.global.logging;

import java.util.Map;
import org.slf4j.MDC;

final class MdcContext {

    private MdcContext() {
    }

    static void replace(Map<String, String> context) {
        if (context == null || context.isEmpty()) {
            MDC.clear();
            return;
        }
        MDC.setContextMap(context);
    }
}
