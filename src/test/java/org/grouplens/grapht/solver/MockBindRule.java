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
package org.grouplens.grapht.solver;

import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.MockQualifierMatcher;
import org.grouplens.grapht.reflect.QualifierMatcher;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * MockBindRule is a simple implementation of BindRule where the matching
 * bindings are represented as a map from input Desires to output Desires, via
 * {@link #addMapping(Desire, Desire)}.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class MockBindRule implements BindRule {
    private final Map<Desire, Desire> bindings;
    private boolean terminate;
    private CachePolicy policy;
    
    public MockBindRule() {
        bindings = new HashMap<Desire, Desire>();
        policy = CachePolicy.NO_PREFERENCE;
    }
    
    public MockBindRule(Desire in, Desire out) {
        this();
        addMapping(in, out);
    }
    
    public MockBindRule setCachePolicy(CachePolicy policy) {
        this.policy = policy;
        return this;
    }
    
    public MockBindRule setTerminatesChain(boolean terminate) {
        this.terminate = terminate;
        return this;
    }
    
    public MockBindRule addMapping(Desire in, Desire out) {
        bindings.put(in, out);
        return this;
    }
    
    @Override
    public CachePolicy getCachePolicy() {
        return policy;
    }
    
    @Override
    public boolean matches(Desire desire) {
        return bindings.containsKey(desire);
    }

    @Override
    public Desire apply(Desire desire) {
        return bindings.get(desire);
    }

    @Override
    public EnumSet<BindingFlag> getFlags() {
        if (terminate) {
            return EnumSet.of(BindingFlag.TERMINAL);
        } else {
            return BindingFlag.emptySet();
        }
    }

    @Override
    public boolean isTerminal() {
        return terminate;
    }
    
    public QualifierMatcher getQualifier() {
        return MockQualifierMatcher.any();
    }

    @Override
    public BindRuleBuilder newCopyBuilder() {
        throw new UnsupportedOperationException("cannot configure mock bind rules");
    }

    public int compareTo(BindRule other) {
        return getQualifier().compareTo(((MockBindRule) other).getQualifier());
    }

    @Override
    public String toString() {
        return getClass() + "@" + Integer.toHexString(System.identityHashCode(this));
    }
}
