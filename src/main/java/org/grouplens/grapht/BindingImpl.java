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
package org.grouplens.grapht;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Provider;

import org.grouplens.grapht.spi.BindRule;
import org.grouplens.grapht.spi.ContextChain;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.reflect.Types;

/**
 * BindingImpl is the default implementation of Binding that is used by
 * {@link InjectorConfigurationBuilder}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 * @param <T> The bindings source's type
 */
class BindingImpl<T> implements Binding<T> {
    private final ContextImpl context;
    private final Class<T> sourceType;
    
    private final Set<Class<?>> excludeTypes;
    
    private final QualifierMatcher qualifier;
    private final boolean terminate;
    
    public BindingImpl(ContextImpl context, Class<T> type) {
        this(context, type, context.getBuilder().getDefaultExclusions(),
             context.getBuilder().getSPI().matchAny(), false);
    }
    
    public BindingImpl(ContextImpl context, Class<T> type, 
                       Set<Class<?>> excludes, QualifierMatcher matcher, 
                       boolean terminateChain) {
        this.context = context;
        sourceType = type;
        excludeTypes = excludes;
        qualifier = matcher;
        terminate = terminateChain;
    }

    @Override
    public Binding<T> withQualifier(Class<? extends Annotation> qualifier) {
        QualifierMatcher q = context.getBuilder().getSPI().match(qualifier);
        return new BindingImpl<T>(context, sourceType, excludeTypes, q, terminate);
    }
    
    @Override
    public Binding<T> withQualifier(Annotation annot) {
        QualifierMatcher q = context.getBuilder().getSPI().match(annot);
        return new BindingImpl<T>(context, sourceType, excludeTypes, q, terminate);
    }
    
    @Override
    public Binding<T> unqualified() {
        QualifierMatcher q = context.getBuilder().getSPI().matchNone();
        return new BindingImpl<T>(context, sourceType, excludeTypes, q, terminate);
    }

    @Override
    public Binding<T> exclude(Class<?> exclude) {
        if (exclude == null) {
            throw new NullPointerException("Type cannot be null");
        }
        Set<Class<?>> excludes = new HashSet<Class<?>>(excludeTypes);
        excludes.add(exclude);
        return new BindingImpl<T>(context, sourceType, excludes, qualifier, terminate);
    }
    
    @Override
    public Binding<T> finalBinding() {
        return new BindingImpl<T>(context, sourceType, excludeTypes, qualifier, true);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void to(Class<? extends T> impl) {
        ContextChain chain = context.getContextChain();
        InjectorConfigurationBuilder config = context.getBuilder();

        if (config.getGenerateRules()) {
            Map<Class<?>, Integer> bindPoints = generateBindPoints(impl);
            for (Entry<Class<?>, Integer> e: bindPoints.entrySet()) {
                BindRule rule = config.getSPI().bindType(qualifier, (Class) e.getKey(), impl, e.getValue(), terminate);
                config.addBindRule(chain, rule);
            }
        } else {
            config.addBindRule(chain, config.getSPI().bindType(qualifier, sourceType, impl, BindRule.MANUAL_BIND_RULE, terminate));
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void to(T instance) {
        ContextChain chain = context.getContextChain();
        InjectorConfigurationBuilder config = context.getBuilder();

        // Apply some type coercing if we're dealing with primitive types
        Object coerced = coerce(instance);
        
        if (config.getGenerateRules()) {
            Map<Class<?>, Integer> bindPoints = generateBindPoints(coerced.getClass());
            for (Entry<Class<?>, Integer> e: bindPoints.entrySet()) {
                BindRule rule = config.getSPI().bindInstance(qualifier, (Class) e.getKey(), coerced, e.getValue());
                config.addBindRule(chain, rule);
            }
        } else {
            config.addBindRule(chain, config.getSPI().bindInstance(qualifier, (Class) sourceType, coerced, BindRule.MANUAL_BIND_RULE));
        }
    }
    
    private Object coerce(Object in) {
        Class<?> boxedSource = Types.box(sourceType);
        if (Integer.class.equals(boxedSource)) {
            // normalize to BigInteger and then cast to int
            return Integer.valueOf(toBigInteger(in).intValue());
        } else if (Short.class.equals(boxedSource)) {
            // normalize to BigInteger and then cast to short
            return Short.valueOf(toBigInteger(in).shortValue());
        } else if (Byte.class.equals(boxedSource)) {
            // normalize to BigInteger and then cast to byte
            return Byte.valueOf(toBigInteger(in).byteValue());
        } else if (Long.class.equals(boxedSource)) {
            // normalize to BigInteger and then cast to long
            return Long.valueOf(toBigInteger(in).longValue());
        } else if (Float.class.equals(boxedSource)) {
            // normalize to BigDecimal and then cast to float
            return Float.valueOf(toBigDecimal(in).floatValue());
        } else if (Double.class.equals(boxedSource)) {
            // normalize to BigDecimal and then cast to double
            return Double.valueOf(toBigDecimal(in).doubleValue());
        } else if (BigDecimal.class.equals(boxedSource)) {
            // normalize to BigDecimal
            return toBigDecimal(in);
        } else if (BigInteger.class.equals(boxedSource)) {
            // normalize to BigInteger
            return toBigInteger(in);
        } else {
            // don't perform any type coercion
            return in;
        }
    }
    
    private BigDecimal toBigDecimal(Object in) {
        // We assume in is a floating primitive boxed type, so its toString()
        // converts its value to a form parsed by BigDecimal's constructor
        return new BigDecimal(in.toString());
    }
    
    private BigInteger toBigInteger(Object in) {
        // We assume in is a discrete primitive boxed type, so its toString()
        // converts its value to a textual form that can be parsed by 
        // BigInteger's constructor
        return new BigInteger(in.toString());
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void toProvider(Class<? extends Provider<? extends T>> provider) {
        ContextChain chain = context.getContextChain();
        InjectorConfigurationBuilder config = context.getBuilder();

        if (config.getGenerateRules()) {
            Map<Class<?>, Integer> bindPoints = generateBindPoints(Types.getProvidedType(provider));
            for (Entry<Class<?>, Integer> e: bindPoints.entrySet()) {
                BindRule rule = config.getSPI().bindProvider(qualifier, (Class) e.getKey(), provider, e.getValue());
                config.addBindRule(chain, rule);
            }
        } else {
            config.addBindRule(chain, config.getSPI().bindProvider(qualifier, sourceType, provider, BindRule.MANUAL_BIND_RULE));
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void toProvider(Provider<? extends T> provider) {
        ContextChain chain = context.getContextChain();
        InjectorConfigurationBuilder config = context.getBuilder();

        if (config.getGenerateRules()) {
            Map<Class<?>, Integer> bindPoints = generateBindPoints(Types.getProvidedType(provider));
            for (Entry<Class<?>, Integer> e: bindPoints.entrySet()) {
                BindRule rule = config.getSPI().bindProvider(qualifier, (Class) e.getKey(), provider, e.getValue());
                config.addBindRule(chain, rule);
            }
        } else {
            config.addBindRule(chain, config.getSPI().bindProvider(qualifier, sourceType, provider, BindRule.MANUAL_BIND_RULE));
        }
    }
    
    private Map<Class<?>, Integer> generateBindPoints(Class<?> target) {
        Map<Class<?>, Integer> bindPoints = new HashMap<Class<?>, Integer>();
        // start the recursion up the type hierarchy, starting at the target type
        recordTypes(Types.box(sourceType), target, bindPoints);
        return bindPoints;
    }
    
    private void recordTypes(Class<?> src, Class<?> type, Map<Class<?>, Integer> bindPoints) {
        // check exclusions
        if (type == null || excludeTypes.contains(type)) {
            // the type is excluded, terminate recursion (this relies on Object
            // being included in the exclude set)
            return;
        }
        
        int weight;
        if (type.equals(src)) {
            // type is the source type, so this is the manual rule
            weight = BindRule.MANUAL_BIND_RULE;
        } else if (src.isAssignableFrom(type)) {
            // type is a subclass of the source type, and a superclass
            // of the target type
            weight = BindRule.FIRST_TIER_GENERATED_BIND_RULE;
        } else if (type.isAssignableFrom(src)) {
            // type is a superclass of the source type, so it is also a superclass
            // of the target type
            weight = BindRule.SECOND_TIER_GENERATED_BIND_RULE;
        } else {
            // type is a superclass of the target type, but not of the source type
            // so we don't generate any bindings
            return;
        }
        
        // record the type's weight
        bindPoints.put(type, weight);
        
        // recurse to superclass and implemented interfaces
        // - superclass is null for Object, interfaces, and primitives
        // - interfaces holds implemented or extended interfaces depending on
        //   if the type is a class or interface
        recordTypes(src, type.getSuperclass(), bindPoints);
        for (Class<?> i: type.getInterfaces()) {
            recordTypes(src, i, bindPoints);
        }
    }
}
