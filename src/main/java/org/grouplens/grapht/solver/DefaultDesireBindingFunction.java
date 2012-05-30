package org.grouplens.grapht.solver;

import java.lang.annotation.Annotation;

import org.grouplens.grapht.annotation.DefaultBoolean;
import org.grouplens.grapht.annotation.DefaultDouble;
import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.grapht.annotation.DefaultInteger;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.grapht.annotation.DefaultString;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

/**
 * A binding function that looks for {@link DefaultImplementation} or
 * {@link DefaultProvider} on the desired type or the qualifier. For constants,
 * it will also check for {@link DefaultDouble}, {@link DefaultInteger},
 * {@link DefaultBoolean}, and {@link DefaultString}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class DefaultDesireBindingFunction implements BindingFunction {
    private final InjectSPI spi;
    
    public DefaultDesireBindingFunction(InjectSPI spi) {
        Preconditions.notNull("spi", spi);
        this.spi = spi;
    }
    
    @Override
    public BindingResult bind(InjectionContext context, Desire desire) throws SolverException {
        Annotation qualifier = desire.getInjectionPoint().getAttributes().getQualifier();

        // Only use qualifier defaults if this is the first desire
        if (context.getPriorDesires().isEmpty()) {
            if (qualifier != null) {
                Class<? extends Annotation> annotType = qualifier.annotationType();
                DefaultDouble dfltDouble = annotType.getAnnotation(DefaultDouble.class);
                if (dfltDouble != null) {
                    return new BindingResult(spi.desire(desire.getInjectionPoint(), spi.satisfy(dfltDouble.value())),
                                             false, true);
                }
                DefaultInteger dfltInt = annotType.getAnnotation(DefaultInteger.class);
                if (dfltInt != null) {
                    return new BindingResult(spi.desire(desire.getInjectionPoint(), spi.satisfy(dfltInt.value())),
                                             false, true);
                }
                DefaultBoolean dfltBool = annotType.getAnnotation(DefaultBoolean.class);
                if (dfltBool != null) {
                    return new BindingResult(spi.desire(desire.getInjectionPoint(), spi.satisfy(dfltBool.value())),
                                             false, true);
                }
                DefaultString dfltStr = annotType.getAnnotation(DefaultString.class);
                if (dfltStr != null) {
                    return new BindingResult(spi.desire(desire.getInjectionPoint(), spi.satisfy(dfltStr.value())),
                                             false, true);
                }
                DefaultProvider provided = annotType.getAnnotation(DefaultProvider.class);
                if (provided != null) {
                    return new BindingResult(spi.desire(desire.getInjectionPoint(), spi.satisfyWithProvider(provided.value())),
                                             false, true);
                }
                DefaultImplementation impl = annotType.getAnnotation(DefaultImplementation.class);
                if (impl != null) {
                    if (Types.isInstantiable(impl.value())) {
                        return new BindingResult(spi.desire(desire.getInjectionPoint(), spi.satisfy(impl.value())),
                                                 false, false);
                    } else {
                        return new BindingResult(desire.restrict(impl.value()), false, false);
                    }
                }
            }
        }
        
        // Now check the desired type for @DefaultImplementation or @DefaultProvider if the type
        // source has not been disabled.
        DefaultProvider provided = desire.getInjectionPoint().getErasedType().getAnnotation(DefaultProvider.class);
        if (provided != null) {
            return new BindingResult(spi.desire(desire.getInjectionPoint(), spi.satisfyWithProvider(provided.value())),
                                     false, true);
        }
        DefaultImplementation impl = desire.getInjectionPoint().getErasedType().getAnnotation(DefaultImplementation.class);
        if (impl != null) {
            if (Types.isInstantiable(impl.value())) {
                return new BindingResult(spi.desire(desire.getInjectionPoint(), spi.satisfy(impl.value())),
                                         false, false);
            } else {
                return new BindingResult(desire.restrict(impl.value()), false, false);
            }
        }
        
        // There are no annotations on the {@link Qualifier} or the type that indicate a
        // default binding or value, or the defaults have been disabled,
        // so we return null
        return new BindingResult(null, false, false);
    }
}
