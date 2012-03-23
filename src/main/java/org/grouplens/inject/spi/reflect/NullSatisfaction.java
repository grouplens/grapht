package org.grouplens.inject.spi.reflect;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.util.InstanceProvider;
import org.grouplens.inject.util.Types;

import com.google.common.base.Function;

/**
 * NullSatisfaction is a satisfaction that explicitly satisfies desires with the
 * <code>null</code> value.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class NullSatisfaction extends ReflectionSatisfaction {
    private final Class<?> type;
    
    /**
     * Create a NullSatisfaction that uses <code>null</code> to satisfy the
     * given class type.
     * 
     * @param type The type to satisfy
     * @throws NullPointerException if type is null
     */
    public NullSatisfaction(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("Type cannot be null");
        }
        this.type = Types.box(type);
    }
    
    @Override
    public boolean canProduceNull() {
        // NullSatisfaction always produces a null value
        return true;
    }
    
    @Override
    public List<? extends Desire> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Class<?> getErasedType() {
        return type;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Provider<?> makeProvider(Function<? super Desire, ? extends Provider<?>> dependencies) {
        return new InstanceProvider(null);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NullSatisfaction)) {
            return false;
        }
        return ((NullSatisfaction) o).type.equals(type);
    }
    
    @Override
    public int hashCode() {
        return type.hashCode();
    }
    
    @Override
    public String toString() {
        return "Null(" + type.getSimpleName() + ")";
    }
}
