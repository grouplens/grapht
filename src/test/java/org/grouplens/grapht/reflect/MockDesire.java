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
package org.grouplens.grapht.reflect;

import java.lang.annotation.Annotation;

/**
 * MockDesire is a simple Desire implementation for use within certain types of
 * tests.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class MockDesire implements Desire {
    private static final long serialVersionUID = 1L;

    private final Annotation qualifier;
    private final Satisfaction satisfaction;
    private final Class<?> desiredType;
    
    public MockDesire() {
        this(null);
    }
    
    public MockDesire(Satisfaction satisfaction) {
        this(satisfaction, null);
    }
    
    public MockDesire(Satisfaction satisfaction, Annotation qualifier) {
        this((satisfaction == null ? Void.class : satisfaction.getErasedType()),
             satisfaction, qualifier);
    }
    
    public MockDesire(Class<?> desiredType, Satisfaction satisfaction, Annotation qualifier) {
        this.desiredType = desiredType;
        this.satisfaction = satisfaction;
        this.qualifier = qualifier;
    }
    
    @Override
    public boolean isInstantiable() {
        return satisfaction != null;
    }

    @Override
    public Satisfaction getSatisfaction() {
        return satisfaction;
    }

    @Override
    public Class<?> getDesiredType() {
        return desiredType;
    }

    @Override
    public InjectionPoint getInjectionPoint() {
        return Desires.createInjectionPoint(qualifier, getDesiredType(), false);
    }

    @Override
    public Desire restrict(Class<?> type) {
        return new MockDesire(type, satisfaction, qualifier);
    }

    @Override
    public Desire restrict(Satisfaction satisfaction) {
        return new MockDesire(satisfaction.getErasedType(), satisfaction, qualifier);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("mock desire for ")
          .append(desiredType);
        if (qualifier != null) {
            sb.append(" with qualifier ")
              .append(qualifier);
        }
        if (satisfaction != null) {
            sb.append(" (satisfied by ")
              .append(satisfaction)
              .append(")");
        }
        return sb.toString();
    }
}
