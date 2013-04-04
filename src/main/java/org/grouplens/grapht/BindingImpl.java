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

import org.grouplens.grapht.BindingFunctionBuilder.RuleSet;
import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.grapht.solver.BindRules;
import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.ElementChainContextMatcher;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * BindingImpl is the default implementation of Binding that is used by
 * {@link BindingFunctionBuilder}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 * @param <T> The bindings source's type
 */
class BindingImpl<T> implements Binding<T> {
    private static final Logger logger = LoggerFactory.getLogger(BindingImpl.class);
    
    private final ContextImpl context;
    private final Class<T> sourceType;
    
    private final Set<Class<?>> excludeTypes;
    
    private final QualifierMatcher qualifier;
    private final boolean terminate;
    
    private final CachePolicy cachePolicy;
    
    public BindingImpl(ContextImpl context, Class<T> type) {
        this(context, type, context.getBuilder().getDefaultExclusions(),
             context.getBuilder().getSPI().matchAny(), 
             CachePolicy.NO_PREFERENCE, false);
    }
    
    public BindingImpl(ContextImpl context, Class<T> type, 
                       Set<Class<?>> excludes, QualifierMatcher matcher, 
                       CachePolicy cachePolicy, boolean terminateChain) {
        this.context = context;
        this.cachePolicy = cachePolicy;
        sourceType = type;
        excludeTypes = excludes;
        qualifier = matcher;
        terminate = terminateChain;
    }

    @Override
    public Binding<T> withQualifier(@Nonnull Class<? extends Annotation> qualifier) {
        QualifierMatcher q = context.getBuilder().getSPI().match(qualifier);
        return new BindingImpl<T>(context, sourceType, excludeTypes, q, cachePolicy, terminate);
    }
    
    @Override
    public Binding<T> withQualifier(@Nonnull Annotation annot) {
        QualifierMatcher q = context.getBuilder().getSPI().match(annot);
        return new BindingImpl<T>(context, sourceType, excludeTypes, q, cachePolicy, terminate);
    }
    
    @Override
    public Binding<T> unqualified() {
        QualifierMatcher q = context.getBuilder().getSPI().matchNone();
        return new BindingImpl<T>(context, sourceType, excludeTypes, q, cachePolicy, terminate);
    }

    @Override
    public Binding<T> exclude(@Nonnull Class<?> exclude) {
        Preconditions.notNull("exclude type", exclude);
        Set<Class<?>> excludes = new HashSet<Class<?>>(excludeTypes);
        excludes.add(exclude);
        return new BindingImpl<T>(context, sourceType, excludes, qualifier, cachePolicy, terminate);
    }
    
    @Override
    public Binding<T> finalBinding() {
        return new BindingImpl<T>(context, sourceType, excludeTypes, qualifier, cachePolicy, true);
    }

    @Override
    public Binding<T> shared() {
        return new BindingImpl<T>(context, sourceType, excludeTypes, qualifier, CachePolicy.MEMOIZE, terminate);
    }
    
    @Override
    public Binding<T> unshared() {
        return new BindingImpl<T>(context, sourceType, excludeTypes, qualifier, CachePolicy.NEW_INSTANCE, terminate);
    }
    
    @Override
    public void to(@Nonnull Class<? extends T> impl) {
        Preconditions.isAssignable(sourceType, impl);
        if (logger.isWarnEnabled()) {
            if (Types.shouldBeInstantiable(impl)
                    && !Types.isInstantiable(impl)
                    && impl.getAnnotation(DefaultProvider.class) == null
                    && impl.getAnnotation(DefaultImplementation.class) == null) {
                logger.warn("Concrete type {} does not have an injectable or public default constructor, but probably should", impl);
            }
        }
        
        boolean useSatisfaction = Types.isInstantiable(impl);
        
        ElementChainContextMatcher matcher = context.getContextChain();
        BindingFunctionBuilder config = context.getBuilder();

        if (config.getGenerateRules()) {
            Map<Class<?>, RuleSet> bindPoints = generateBindPoints(impl);
            for (Entry<Class<?>, RuleSet> e: bindPoints.entrySet()) {
                if (useSatisfaction) {
                    config.addBindRule(e.getValue(), matcher,
                                       BindRules.toSatisfaction(e.getKey(), qualifier, config.getSPI().satisfy(impl),
                                                                cachePolicy, terminate));
                } else {
                    config.addBindRule(e.getValue(), matcher,
                                       BindRules.toClass(e.getKey(), qualifier, impl,
                                                         cachePolicy, terminate));
                }
            }
        } else {
            if (useSatisfaction) {
                config.addBindRule(RuleSet.EXPLICIT, matcher,
                                   BindRules.toSatisfaction(sourceType, qualifier, config.getSPI().satisfy(impl),
                                                            cachePolicy, terminate));
            } else {
                config.addBindRule(RuleSet.EXPLICIT, matcher,
                                   BindRules.toClass(sourceType, qualifier, impl,
                                                     cachePolicy, terminate));
            }
        }
    }

    @Override
    public void to(@Nullable T instance) {
        if (instance == null) {
            toNull();
            return;
        }
        ElementChainContextMatcher matcher = context.getContextChain();
        BindingFunctionBuilder config = context.getBuilder();

        // Apply some type coercing if we're dealing with primitive types
        Object coerced = coerce(instance);
        Satisfaction s = config.getSPI().satisfy(coerced);
        
        if (config.getGenerateRules()) {
            Map<Class<?>, RuleSet> bindPoints = generateBindPoints(coerced.getClass());
            for (Entry<Class<?>, RuleSet> e: bindPoints.entrySet()) {
                config.addBindRule(e.getValue(), matcher,
                                   BindRules.toSatisfaction(e.getKey(), qualifier, s, cachePolicy, true));
            }
        } else {
            config.addBindRule(RuleSet.EXPLICIT, matcher,
                               BindRules.toSatisfaction(sourceType, qualifier, s, cachePolicy, true));
        }
    }
    
    @Override
    public void toProvider(@Nonnull Class<? extends Provider<? extends T>> provider) {
        ElementChainContextMatcher matcher = context.getContextChain();
        BindingFunctionBuilder config = context.getBuilder();
        Satisfaction s = config.getSPI().satisfyWithProvider(provider);
        
        if (config.getGenerateRules()) {
            Map<Class<?>, RuleSet> bindPoints = generateBindPoints(Types.getProvidedType(provider));
            for (Entry<Class<?>, RuleSet> e: bindPoints.entrySet()) {
                config.addBindRule(e.getValue(), matcher,
                                   BindRules.toSatisfaction(e.getKey(), qualifier, s, cachePolicy, true));
            }
        } else {
            config.addBindRule(RuleSet.EXPLICIT, matcher,
                               BindRules.toSatisfaction(sourceType, qualifier, s, cachePolicy, true));
        }
    }

    @Override
    public void toProvider(@Nonnull Provider<? extends T> provider) {
        ElementChainContextMatcher matcher = context.getContextChain();
        BindingFunctionBuilder config = context.getBuilder();
        Satisfaction s = config.getSPI().satisfyWithProvider(provider);

        if (config.getGenerateRules()) {
            Map<Class<?>, RuleSet> bindPoints = generateBindPoints(Types.getProvidedType(provider));
            for (Entry<Class<?>, RuleSet> e: bindPoints.entrySet()) {
                config.addBindRule(e.getValue(), matcher,
                                   BindRules.toSatisfaction(e.getKey(), qualifier, s, cachePolicy, true));
            }
        } else {
            config.addBindRule(RuleSet.EXPLICIT, matcher,
                               BindRules.toSatisfaction(sourceType, qualifier, s, cachePolicy, true));
        }
    }

    @Override
    public void toNull() {
        toNull(sourceType);
    }

    @Override
    public void toNull(Class<? extends T> type) {
        ElementChainContextMatcher matcher = context.getContextChain();
        BindingFunctionBuilder config = context.getBuilder();

        Satisfaction s = config.getSPI().satisfyWithNull(type);

        if (config.getGenerateRules()) {
            Map<Class<?>, RuleSet> bindPoints = generateBindPoints(type);
            for (Entry<Class<?>, RuleSet> e: bindPoints.entrySet()) {
                config.addBindRule(e.getValue(), matcher,
                                   BindRules.toSatisfaction(e.getKey(), qualifier, s, cachePolicy, true));
            }
        } else {
            config.addBindRule(RuleSet.EXPLICIT, matcher,
                               BindRules.toSatisfaction(sourceType, qualifier, s, cachePolicy, true));
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
    
    private Map<Class<?>, RuleSet> generateBindPoints(Class<?> target) {
        Map<Class<?>, RuleSet> bindPoints = new HashMap<Class<?>, RuleSet>();
        // start the recursion up the type hierarchy, starting at the target type
        recordTypes(Types.box(sourceType), target, bindPoints);
        return bindPoints;
    }
    
    private void recordTypes(Class<?> src, Class<?> type, Map<Class<?>, RuleSet> bindPoints) {
        // check exclusions
        if (type == null || excludeTypes.contains(type)) {
            // the type is excluded, terminate recursion (this relies on Object
            // being included in the exclude set)
            return;
        }
        
        RuleSet set;
        if (type.equals(src)) {
            // type is the source type, so this is the manual rule
            set = RuleSet.EXPLICIT;
        } else if (src.isAssignableFrom(type)) {
            // type is a subclass of the source type, and a superclass
            // of the target type
            set = RuleSet.INTERMEDIATE_TYPES;
        } else if (type.isAssignableFrom(src)) {
            // type is a superclass of the source type, so it is also a superclass
            // of the target type
            set = RuleSet.SUPER_TYPES;
        } else {
            // type is a superclass of the target type, but not of the source type
            // so we don't generate any bindings
            return;
        }
        
        // record the type's weight
        bindPoints.put(type, set);
        
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
