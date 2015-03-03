/**
 * Interface for injection point visitors.  Instances of this interface are used to inspect or act on different types of injection points.
 *
 * @see InjectionPoint#accept(InjectionPointVisitor)
 */
package org.grouplens.grapht.reflect;


import org.grouplens.grapht.ConstructionException;
import org.grouplens.grapht.Instantiator;
import org.grouplens.grapht.reflect.internal.FieldInjectionPoint;
import org.grouplens.grapht.reflect.internal.InjectionPointVisitorImpl;
import org.grouplens.grapht.reflect.internal.NoArgumentInjectionPoint;
import org.grouplens.grapht.reflect.internal.SetterInjectionPoint;
import java.util.Map;

public interface InjectionPointVisitor {

    void visitField(FieldInjectionPoint ip) throws ConstructionException;

    void visitSetter(SetterInjectionPoint ip) throws ConstructionException;

    void visitNoArgument(NoArgumentInjectionPoint ip) throws ConstructionException;

    void visitConstructor() throws ConstructionException;
}
