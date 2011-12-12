/*
 * LensKit, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit.inject.graph;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.grouplens.inject.graph.BindRule;
import org.grouplens.inject.graph.Desire;
import org.grouplens.inject.graph.Node;

/**
 * MockDesire is a simple Desire implementation for use within certain types of
 * tests. The satisfiable nodes and desires can be configured by mutating the
 * sets returned by {@link #getSatisfiableDesires()} and
 * {@link #getSatisfiableNodes()}.
 * 
 * @author Michael Ludwig
 */
public class MockDesire implements Desire {
    private final Class<? extends Annotation> parameterOrRole;
    
    private final boolean isParameter;
    private final Node node;
    
    private final Set<Node> satisfiableNodes;
    private final Set<Desire> satisfiableDesires;
    
    public MockDesire() {
        this(null);
    }
    
    public MockDesire(Node node) {
        this(node, null);
    }
    
    public MockDesire(Node node, Class<? extends Annotation> role) {
        this(node, role, false);
    }
    
    public MockDesire(Node node, Class<? extends Annotation> parameterOrRole,  boolean isParameter) {
        this.node = node;
        this.isParameter = isParameter;
        this.parameterOrRole = parameterOrRole;
        
        this.satisfiableDesires = new HashSet<Desire>();
        this.satisfiableNodes = new HashSet<Node>();
    }
    
    public Set<Node> getSatisfiableNodes() {
        return satisfiableNodes;
    }
    
    public Set<Desire> getSatisfiableDesires() {
        return satisfiableDesires;
    }
    
    @Override
    public boolean isSatisfiedBy(Node node) {
        return satisfiableNodes.contains(node);
    }

    @Override
    public boolean isSatisfiedBy(Desire desire) {
        return satisfiableDesires.contains(desire);
    }

    @Override
    public boolean isParameter() {
        return isParameter;
    }

    @Override
    public Class<? extends Annotation> getRoleAnnotation() {
        return (isParameter ? parameterOrRole : null);
    }

    @Override
    public Class<? extends Annotation> getParameterAnnotation() {
        return (isParameter ? null : parameterOrRole);
    }

    @Override
    public boolean isInstantiable() {
        return node != null;
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public Comparator<BindRule> ruleComparator() {
        return new Comparator<BindRule>() {
            @Override
            public int compare(BindRule o1, BindRule o2) {
                return 0;
            }
        };
    }
}
