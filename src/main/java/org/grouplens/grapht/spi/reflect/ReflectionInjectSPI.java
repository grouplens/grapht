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
package org.grouplens.grapht.spi.reflect;

import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.context.ContextElementMatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.net.URL;

/**
 * ReflectionInjectSPI is a complete implementation of {@link InjectSPI}. It
 * uses Java's reflection API to find constructor and setter method injection
 * points that have been annotated with {@link Inject} to determine a type's
 * dependencies.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class ReflectionInjectSPI implements InjectSPI {
    protected final ClassLoader classLoader;

    public ReflectionInjectSPI() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ReflectionInjectSPI.class.getClassLoader();
        }
        classLoader = loader;
    }

    private ReflectionInjectSPI(ClassLoader loader) {
        classLoader = loader;
    }

    public static InjectSPI forClassLoader(ClassLoader loader) {
        return new ReflectionInjectSPI(loader);
    }

    @Override
    public ContextElementMatcher contextElement(QualifierMatcher qualifier, Class<?> type) {
        return new ReflectionContextElementMatcher(type, qualifier);
    }
    
    @Override
    public Desire desire(@Nullable Annotation qualifier, Class<?> type, boolean nullable) {
        return new ReflectionDesire(new SimpleInjectionPoint(qualifier, type, nullable));
    }
    
    @Override
    public QualifierMatcher match(Class<? extends Annotation> qualifier) {
        if (qualifier != null) {
            return Qualifiers.match(qualifier);
        } else {
            return Qualifiers.matchNone();
        }
    }

    @Override
    public QualifierMatcher match(Annotation annot) {
        if (annot != null) {
            return Qualifiers.match(annot);
        } else {
            return Qualifiers.matchNone();
        }
    }

    @Override
    public QualifierMatcher matchDefault() {
        return Qualifiers.matchDefault();
    }

    @Override
    public QualifierMatcher matchAny() {
        return Qualifiers.matchAny();
    }

    @Override
    public QualifierMatcher matchNone() {
        return Qualifiers.matchNone();
    }

    @Override
    public Satisfaction satisfy(@Nonnull Class<?> type) {
        return new ClassSatisfaction(type);
    }

    @Override
    public Satisfaction satisfyWithNull(@Nonnull Class<?> type) {
        return new NullSatisfaction(type);
    }

    @Override
    public Satisfaction satisfy(@Nonnull Object o) {
        return new InstanceSatisfaction(o);
    }

    @Override
    public Satisfaction satisfyWithNamedType(@Nonnull String name) throws ClassNotFoundException {
        Class<?> clazz = classLoader.loadClass(name);
        return satisfy(clazz);
    }

    @Override
    public Satisfaction satisfyWithProvider(@Nonnull Class<? extends Provider<?>> providerType) {
        return new ProviderClassSatisfaction(providerType);
    }

    @Override
    public Satisfaction satisfyWithProvider(@Nonnull Provider<?> provider) {
        return new ProviderInstanceSatisfaction(provider);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Satisfaction satisfyWithProvider(@Nonnull String providerName) throws ClassNotFoundException {
        Class<?> clazz = classLoader.loadClass(providerName);
        return satisfyWithProvider((Class<Provider<?>>) clazz.asSubclass(Provider.class));
    }

    @Override
    public URL getResource(String path) {
        return classLoader.getResource(path);
    }
}
