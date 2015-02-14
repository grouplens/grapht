package org.grouplens.grapht.util;

import java.util.Collections;
import java.util.Map;
import org.slf4j.MDC;

/**
 * Utility class to manage log data with {@link MDC}.  This class allows MDC parameters
 * to be set, and popped back off when the context is finished.
 */

public class LogContext {
    @SuppressWarnings("rawtypes")

    private final Map memory = MDC.getCopyOfContextMap();

    private  LogContext() {}

    /**
     * Method creates a new log context,
     * capturing the MDC's current data to be
     * restored when finish() is called.
     */
    static public  LogContext create() {
        return new LogContext();
    }

    /**
     * Set a key in the MDC environment.
     * @param key The key to set.
     * @param value The key's value.
     * @see MDC#put(String, String)
     */
    public void put(String key, String value) {
        MDC.put(key, value);
    }

    /**
     * Finish the context.  This restores the MDC context map to the value it had when the
     * log context was created.
     */
    public void finish() {
        MDC.setContextMap(memory == null ? Collections.EMPTY_MAP : memory);
    }
}

