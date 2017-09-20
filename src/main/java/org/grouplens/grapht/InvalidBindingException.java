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
package org.grouplens.grapht;

import javax.inject.Qualifier;

/**
 * Thrown when a binding configuration is invalid, which often occurs when an
 * implementation type is bound to a type that it is not a subclass of, or when
 * an annotation is intended to be used as a qualifier but has not been
 * annotated with {@link Qualifier}.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class InvalidBindingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Class<?> type;
    
    public InvalidBindingException(Class<?> type) {
        this(type, "");
    }
    
    public InvalidBindingException(Class<?> type, String message) {
        this(type, message, null);
    }
    
    public InvalidBindingException(Class<?> type, Throwable t) {
        this(type, "", t);
    }
    
    public InvalidBindingException(Class<?> type, String message, Throwable t) {
        super(message, t);
        this.type = type;
    }
    
    /**
     * @return The type that is configured incorrectly, or is the cause of
     *         configuration errors
     */
    public Class<?> getType() {
        return type;
    }
    
    @Override
    public String getMessage() {
        return String.format("Error configuring %s: %s", type, super.getMessage());
    }
}
