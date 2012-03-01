/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.inject.spi.reflect;

import java.lang.annotation.Annotation;

import javax.annotation.Nullable;
import javax.inject.Provider;

import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.ContextMatcher;
import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.InjectSPI;

public class ReflectionInjectSPI implements InjectSPI {
    @Override
    public <T> BindRule bindType(Class<? extends Annotation> role, Class<T> source,
                                 Class<? extends T> impl, int weight) {
        return new ClassBindRule(impl, source, role(role), weight);
    }

    @Override
    public <T> BindRule bindInstance(Class<? extends Annotation> role, Class<T> source, T instance, int weight) {
        return new InstanceBindRule(instance, source, role(role), weight);
    }

    @Override
    public <T> BindRule bindProvider(Class<? extends Annotation> role, Class<T> source,
                                     Class<? extends Provider<? extends T>> providerType, int weight) {
        return new ProviderClassBindRule(providerType, source, role(role), weight);
    }

    @Override
    public <T> BindRule bindProvider(Class<? extends Annotation> role, Class<T> source,
                                     Provider<? extends T> provider, int weight) {
        return new ProviderInstanceBindRule(provider, source, role(role), weight);
    }

    @Override
    public ContextMatcher context(Class<? extends Annotation> role, Class<?> type) {
        return new ReflectionContextMatcher(type, role(role));
    }
    
    @Override
    public Desire desire(final @Nullable Class<? extends Annotation> role, final Class<?> type) {
        final AnnotationRole realRole = role(role);
        return new ReflectionDesire(new InjectionPoint() {
            @Override
            public boolean isTransient() {
                return false;
            }
            
            @Override
            public Class<?> getType() {
                return type;
            }
            
            @Override
            public AnnotationRole getRole() {
                return realRole;
            }
        });
    }
    
    private AnnotationRole role(Class<? extends Annotation> role) {
        return (role == null ? null : new AnnotationRole(role));
    }
}
