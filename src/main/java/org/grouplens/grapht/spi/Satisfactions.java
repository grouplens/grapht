package org.grouplens.grapht.spi;

import org.grouplens.grapht.spi.reflect.*;

import javax.annotation.Nonnull;
import javax.inject.Provider;

/**
 * Class to construct specific {@link Satisfaction} implementations.
 *
 * @since 0.7
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class Satisfactions {
    private Satisfactions() {}

    public static Satisfaction satisfy(@Nonnull Class<?> type) {
        return new ClassSatisfaction(type);
    }

    public static Satisfaction satisfyWithNull(@Nonnull Class<?> type) {
        return new NullSatisfaction(type);
    }

    public static Satisfaction satisfy(@Nonnull Object o) {
        return new InstanceSatisfaction(o);
    }

    public static Satisfaction satisfyWithProvider(@Nonnull Class<? extends Provider<?>> providerType) {
        return new ProviderClassSatisfaction(providerType);
    }

    public static Satisfaction satisfyWithProvider(@Nonnull Provider<?> provider) {
        return new ProviderInstanceSatisfaction(provider);
    }
}
