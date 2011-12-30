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
package org.grouplens.inject.resolver;

import javax.annotation.Nullable;

import org.grouplens.inject.graph.Desire;
import org.grouplens.inject.graph.Node;
import org.grouplens.inject.graph.Role;

/**
 * NodeAndRole is a simple pairing between a Node and a Role. Roles are defined
 * as part of a dependency {@link Desire}. However, when resolving all immediate
 * and transient dependencies for a Node, it can be useful to keep track of the
 * Role of the desire that the Node is satisfying.
 * 
 * @see ContextMatcher
 * @see ContextChain
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class NodeAndRole {
    private final Node node;
    private final Role role;

    /**
     * Create a new NodeAndRole pair between the given Node and Role. The role
     * can be null to represent the default role.
     * 
     * @param node The Node satisfying some dependency or desire
     * @param role The role on the desire that the node satisfies
     * @throws NullPointerException if node is null
     */
    public NodeAndRole(Node node, @Nullable Role role) {
        if (node == null)
            throw new NullPointerException("Node cannot be null");
        
        this.node = node;
        this.role = role;
    }
    
    /**
     * @return The Node satisfying some desire, will not be null
     */
    public Node getNode() {
        return node;
    }
    
    /**
     * @return The role on the desire that the Node satisfies, may be null
     */
    public Role getRole() {
        return role;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NodeAndRole))
            return false;
        NodeAndRole n = (NodeAndRole) o;
        return n.node.equals(node) && (n.role == null ? role == null : n.role.equals(role));
    }
}