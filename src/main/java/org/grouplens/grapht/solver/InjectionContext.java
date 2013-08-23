/*
 * Grapht, an open source dependency injector.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.grapht.solver;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.Preconditions;

import java.io.Serializable;
import java.util.*;

/**
 * <p>
 * InjectionContext represents the current path through the dependency graph to
 * the desire being resolved by
 * {@link BindingFunction#bind(InjectionContext, Desire)}. The InjectionContext
 * is most significantly represented as a list of satisfactions and the
 * associated injection point attributes. This list represents the "type path"
 * from the root node in the graph to the previously resolved satisfaction.
 * <p>
 * Although the type path for an InjectionContext instance is immutable, it does
 * maintain mutable state to assist BindingFunction implementations. When
 * resolving a dependency desire, the BindingFunctions might produce a chain of
 * desires before reaching an instantiable one. This chain is recorded as
 * mutable state within a context instance. To allow functions more flexibility,
 * each context instance provides a String-based map.
 * <p>
 * Essentially, each InjectionContext instance is associated with a single
 * resolution attempt for an injection point.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class InjectionContext implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final List<Pair<Satisfaction, Attributes>> context;
    private final transient Map<String, Object> values;
    
    /**
     * Create a new InjectionContext that has an empty context.
     */
    private InjectionContext() {
        // The default context starts out with an empty type path, no prior
        // desires and no stored values
        context = Collections.emptyList();
        values = new HashMap<String, Object>();
    }
    
    private InjectionContext(InjectionContext prior, Satisfaction satisfaction, Attributes attrs) {
        // A context with a pushed satisfaction inherits and updates the type
        // path, but resets the desires and stored values
        List<Pair<Satisfaction, Attributes>> newCtx = new ArrayList<Pair<Satisfaction, Attributes>>(prior.context);
        newCtx.add(Pair.of(satisfaction, attrs));
        
        context = Collections.unmodifiableList(newCtx);
        values = new HashMap<String, Object>();
    }

    /**
     * Create a new empty injection context.
     * @return An empty injection context.
     */
    public static InjectionContext empty() {
        return new InjectionContext();
    }
    
    /**
     * Create a new context that is updated to have the satisfaction and attribute pushed to the
     * end of its type path. The value cache for the new context will be empty.
     * 
     * @param satisfaction The next satisfaction in the dependency graph
     * @param attrs The attributes of the injection point receiving the
     *            satisfaction
     * @return A new context with updated type path
     */
    public InjectionContext extend(Satisfaction satisfaction, Attributes attrs) {
        return new InjectionContext(this, satisfaction, attrs);
    }
    
    /**
     * @return The type path of this context, usable by {@link org.grouplens.grapht.spi.ContextElementMatcher}
     */
    public List<Pair<Satisfaction, Attributes>> getTypePath() {
        return context;
    }
    
    /**
     * Retrieve the object associated with the given String key. This will
     * return null if there is no value associated with the key.
     * 
     * @param key The String key
     * @return The value associated to key by a BindingFunction for this context
     *         instance
     * @throws NullPointerException if key is null
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        Preconditions.notNull("key", key);
        return (T) values.get(key);
    }
    
    /**
     * <p>
     * Associated <tt>value</tt> with <tt>key</tt> for this context instance.
     * This can be used to store values that might affect behavior of a future
     * invocation of {@link BindingFunction#bind(InjectionContext, Desire)} with
     * this context instance.
     * <p>
     * One such example would be to ensure that a BindRule is not applied more
     * than once within a given context.
     * 
     * @param key The String key
     * @param value The value to store
     * @throws NullPointerException if key is null
     */
    public void putValue(String key, Object value) {
        Preconditions.notNull("key", key);
        values.put(key, value);
    }
}
