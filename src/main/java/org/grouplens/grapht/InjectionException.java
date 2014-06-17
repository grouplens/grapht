package org.grouplens.grapht;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class InjectionException extends Exception {
    public InjectionException() {
    }

    public InjectionException(String message) {
        super(message);
    }

    public InjectionException(Throwable cause) {
        super(cause);
    }

    public InjectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
