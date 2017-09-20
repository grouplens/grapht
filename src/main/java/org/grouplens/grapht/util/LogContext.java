/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.grapht.util;

import java.util.Collections;
import java.util.Map;
import org.slf4j.MDC;

/**
 * Utility class to manage log data with {@link MDC}.  This class allows MDC parameters
 * to be set, and popped back off when the context is finished.
 */

public class LogContext implements AutoCloseable {
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

    @Override
    public void close() {
        finish();
    }
}

