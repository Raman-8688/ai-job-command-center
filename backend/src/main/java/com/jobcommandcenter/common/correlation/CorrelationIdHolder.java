package com.jobcommandcenter.common.correlation;

import org.slf4j.MDC;

/**
 * Accessor for the active request correlation ID stored in SLF4J MDC.
 */
public final class CorrelationIdHolder {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String MDC_KEY = "correlationId";

    private CorrelationIdHolder() {
    }

    public static String getCorrelationId() {
        String id = MDC.get(MDC_KEY);
        return id != null ? id : "none";
    }

    public static void setCorrelationId(String correlationId) {
        if (correlationId != null) {
            MDC.put(MDC_KEY, correlationId);
        } else {
            MDC.remove(MDC_KEY);
        }
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }
}
