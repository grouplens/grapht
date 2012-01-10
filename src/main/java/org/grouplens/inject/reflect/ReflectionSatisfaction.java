package org.grouplens.inject.reflect;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.inject.resolver.ContextMatcher;
import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.Role;
import org.grouplens.inject.spi.Satisfaction;

import com.google.common.base.Function;

// There is a good chance that this will be needed because I think that 
// contextComparator() will be shared by most satisfaction implementations.
abstract class ReflectionSatisfaction implements Satisfaction {
    @Override
    public Comparator<ContextMatcher> contextComparator(Role role) {
        // TODO Auto-generated method stub
        return null;
    }
}
