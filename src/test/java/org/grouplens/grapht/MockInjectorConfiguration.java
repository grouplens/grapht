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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.grouplens.grapht.InjectorConfiguration;
import org.grouplens.grapht.spi.BindRule;
import org.grouplens.grapht.spi.ContextChain;
import org.grouplens.grapht.spi.InjectSPI;

/**
 * MockInjectorConfiguration wraps an existing map of bind rules.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class MockInjectorConfiguration implements InjectorConfiguration {
    private final Map<ContextChain, Collection<? extends BindRule>> rules;
    private final InjectSPI spi;
    
    public MockInjectorConfiguration(InjectSPI spi, Map<ContextChain, Collection<? extends BindRule>> rules) {
        this.rules = Collections.unmodifiableMap(rules);
        this.spi = spi;
    }
    
    @Override
    public Map<ContextChain, Collection<? extends BindRule>> getBindRules() {
        return rules;
    }

    @Override
    public InjectSPI getSPI() {
        return spi;
    }
}
