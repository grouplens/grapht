package org.grouplens.inject.spi.reflect;

import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.Desire;

abstract class ReflectionBindRule implements BindRule {
    private final AnnotationRole role;
    private final Class<?> sourceType;
    
    private final boolean generated;
    
    public ReflectionBindRule(AnnotationRole role, Class<?> sourceType, boolean generated) {
        this.role = role;
        this.sourceType = sourceType;
        this.generated = generated;
    }

    /**
     * @return True if this was a generated binding, false if the binding was
     *         specified manually by a programmer or config file
     */
    public boolean isGenerated() {
        return generated;
    }
    
    @Override
    public boolean matches(Desire desire) {
        ReflectionDesire rd = (ReflectionDesire) desire;
        // bind rules match type by equality
        if (rd.getDesiredType().equals(sourceType)) {
            // if the type is equal, then the roles match if
            // the desires role is a subtype of the bind rules role
            return AnnotationRole.inheritsRole(rd.getRole(), role);
        }
        
        // the type and roles are not a match, so return false
        return false;
    }
}
