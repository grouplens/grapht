package org.grouplens.inject.spi;

import javax.annotation.Nullable;

/**
 * MockRole is a simple Role implementation that represents roles as a String.
 * Every MockRole with the same string is considered to be the same role.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class MockRole implements Role {
    private final MockRole parent;
    private final String role;
    
    public MockRole(String role) {
        this(role, null);
    }
    
    public MockRole(String role, @Nullable MockRole parent) {
        this.role = role;
        this.parent = parent;
    }
    
    public MockRole getParent() {
        return parent;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MockRole))
            return false;
        return ((MockRole) o).role.equals(role);
    }
    
    @Override
    public int hashCode() {
        return role.hashCode();
    }
}
