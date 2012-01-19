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
package org.grouplens.inject.spi;

import java.lang.annotation.Annotation;

import javax.annotation.Nullable;
import javax.inject.Provider;


public interface InjectSPI {
    <T> BindRule bind(@Nullable Class<? extends Annotation> role, Class<T> source, 
                      Class<? extends T> impl, int weight);
    
    <T> BindRule bind(@Nullable Class<? extends Annotation> role, Class<T> source,
                      T instance, int weight);
    
    <T> BindRule bindProvider(@Nullable Class<? extends Annotation> role, Class<T> source, 
                              Class<? extends Provider<? extends T>> providerType, int weight);
    
    <T> BindRule bindProvider(@Nullable Class<? extends Annotation> role, Class<T> source, 
                              Provider<? extends T> provider, int weight);
    
    ContextMatcher context(@Nullable Class<? extends Annotation> role, Class<?> type);
    
    // FIXME: Do I need to add desires and satisfaction handling here?
}
