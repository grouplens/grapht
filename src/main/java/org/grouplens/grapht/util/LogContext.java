/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
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

