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
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.Satisfaction;

/**
 * Thrown by {@link BindingFunction} or {@link DependencySolver} when a desire
 * cannot be satisfied.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class SolverException extends Exception {
    private static final long serialVersionUID = 1L;

    public SolverException() {
        super();
    }

    public SolverException(String msg) {
        super(msg);
    }

    public SolverException(Throwable throwable) {
        super(throwable);
    }

    public SolverException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
    
    protected String format(InjectionContext ctx, DesireChain desires) {
        StringBuilder sb = new StringBuilder();
        
        // type path
        sb.append("Context:\n");
        sb.append("  Type path:\n");
        for (Pair<Satisfaction, InjectionPoint> path: ctx) {
            Satisfaction sat = path.getLeft();
            Class<?> type = sat == null ? null : sat.getErasedType();
            sb.append("    ")
              .append(format(path.getRight(), type))
              .append('\n');
        }
        sb.append('\n');
        
        // desire chain
        sb.append("  Prior desires:\n");
        for (Desire desire: desires.getPreviousDesires()) {
            sb.append("    ")
              .append(format(desire.getInjectionPoint(), desire.getDesiredType()))
              .append('\n');
        }

        return sb.toString();
    }

    protected String format(InjectionPoint ip) {
        return format(ip, ip.getErasedType());
    }

    protected String format(InjectionPoint ip, Class<?> type) {
        if (type == null) {
            type = ip.getErasedType();
        }
        String base = (ip.getQualifier() != null ? ip.getQualifier() + ":" : "");
        String name = type == null ? null : type.getName();
        return base + name;
    }
}
