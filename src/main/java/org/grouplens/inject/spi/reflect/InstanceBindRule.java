package org.grouplens.inject.spi.reflect;

import org.grouplens.inject.spi.Desire;

class InstanceBindRule<T> extends ReflectionBindRule {
    private final T instance;
    
    public InstanceBindRule(T instance, AnnotationRole role, Class<? super T> sourceType, boolean generated) {
        super(role, sourceType, generated);
        if (instance == null) {
            throw new NullPointerException("Binding instance cannot be null");
        }
        this.instance = instance;
    }

    @Override
    public Desire apply(Desire desire) {
        ReflectionDesire origDesire = (ReflectionDesire) desire;
        return new ReflectionDesire(instance.getClass(), origDesire.getInjectionPoint(), new InstanceSatisfaction(instance));
    }
    
    // FIXME: document and implement equals/hashCode
}
