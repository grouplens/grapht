/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.grapht.reflect.internal;

import com.google.common.collect.Lists;
import org.grouplens.grapht.InvalidBindingException;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.util.ClassProxy;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

import javax.inject.Inject;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * ReflectionDesire is an implementation of desire that contains all necessary
 * implementation to represent a desire, except that the point of injection is
 * abstracted by an {@link InjectionPoint}.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class ReflectionDesire implements Desire, Serializable {
    private static final long serialVersionUID = -1L;

    /**
     * Return a list of desires that must satisfied in order to instantiate the
     * given type.
     *
     * @param type The class type whose dependencies will be queried
     * @return The dependency desires for the given type
     * @throws NullPointerException if the type is null
     */
    public static List<Desire> getDesires(Class<?> type) {
        List<Desire> desires = Lists.newArrayList();

        boolean ctorFound = false;
        for (Constructor<?> ctor: type.getDeclaredConstructors()) {
            if (ctor.getAnnotation(Inject.class) != null) {
                if (!ctorFound) {
                    ctorFound = true;
                    for (int i = 0; i < ctor.getParameterCount(); i++) {
                        desires.add(forParameter(ctor, i));
                    }
                } else {
                    // at the moment there can only be one injectable constructor
                    throw new InvalidBindingException(type, "More than one constructor with @Inject is not allowed");
                }
            }
        }

        for (Field f: Types.getAllFields(type)) {
            if (f.getAnnotation(Inject.class) != null && !Modifier.isStatic(f.getModifiers())) {
                desires.add(new ReflectionDesire(new FieldInjectionPoint(f)));
            }
        }

        for (Method m: Types.getUniqueMethods(type)) {
            if (m.getAnnotation(Inject.class) != null && !Modifier.isStatic(m.getModifiers())) {
                for (int i = 0; i < m.getParameterCount(); i++) {
                    desires.add(forParameter(m, i));
                }
            }
        }

        return Collections.unmodifiableList(desires);
    }

    static Desire forParameter(Executable member, int param) {
        InjectionPoint ip = new ParameterInjectionPoint(member, param);
        if (member.getParameterTypes()[param].equals(Optional.class)) {
            ip = new OptionalInjectionPoint(ip);
        }
        return new ReflectionDesire(ip);
    }
    
    private final transient Class<?> desiredType;
    private final transient InjectionPoint injectPoint;
    private final transient Satisfaction satisfaction;

    /**
     * Create a ReflectionDesire that immediately wraps the given
     * InjectionPoint. The desired type equals the type declared by the
     * injection point. The created desire will have a satisfaction if the
     * injection point's type is satisfiable.
     * 
     * @param injectPoint The injection point to wrap
     * @throws NullPointerException if injectPoint is null
     */
    public ReflectionDesire(InjectionPoint injectPoint) {
        this(injectPoint.getErasedType(), injectPoint, null);
    }

    /**
     * Create a ReflectionDesire that represents the dependency for
     * <tt>desiredType</tt> that will be injected into the given InjectionPoint.
     * The optional satisfaction will satisfy this desire. If null is provided,
     * and the desired type instantiable, a ClassSatisfaction is created.
     * 
     * @param desiredType The desired type of the dependency
     * @param injectPoint The injection point of the desire
     * @param satisfaction The satisfaction satisfying this desire, if there is
     *            one
     * @throws NullPointerException if desiredType, injectPoint, or dfltSource is null
     * @throws IllegalArgumentException if desiredType is not assignable to the
     *             type of the injection point, or if the satisfaction's type is
     *             not assignable to the desired type
     */
    public ReflectionDesire(Class<?> desiredType, InjectionPoint injectPoint,
                            Satisfaction satisfaction) {
        Preconditions.notNull("desired type", desiredType);
        Preconditions.notNull("injection point", injectPoint);

        desiredType = Types.box(desiredType);
        Preconditions.isAssignable(injectPoint.getErasedType(), desiredType);
        if (satisfaction != null) {
            Preconditions.isAssignable(desiredType, satisfaction.getErasedType());
        }

        // try and find a satisfaction
        if (satisfaction == null) {
            if (Types.shouldBeInstantiable(desiredType)) {
                if (Types.isInstantiable(desiredType)) {
                    satisfaction = new ClassSatisfaction(desiredType);
                } // else don't satisfy this, even if the injection point is null
            } else if (injectPoint.isOptional()) {
                // we only default to null if the injection point depended on
                // an interface. if they ask for a concrete type, it's a bug
                // for that type not to be injectable
                satisfaction = new NullSatisfaction(desiredType);
            }
        }

        this.desiredType = desiredType;
        this.injectPoint = injectPoint;
        this.satisfaction = satisfaction;
    }

    @Override
    public Class<?> getDesiredType() {
        return desiredType;
    }

    @Override
    public InjectionPoint getInjectionPoint() {
        return injectPoint;
    }

    @Override
    public boolean isInstantiable() {
        return satisfaction != null;
    }

    @Override
    public Satisfaction getSatisfaction() {
        return satisfaction;
    }
    
    @Override
    public Desire restrict(Class<?> type) {
        return new ReflectionDesire(type, injectPoint, null);
    }
    
    @Override
    public Desire restrict(Satisfaction satis) {
        return new ReflectionDesire(satis.getErasedType(), injectPoint, satis);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReflectionDesire)) {
            return false;
        }
        ReflectionDesire r = (ReflectionDesire) o;
        return (r.desiredType.equals(desiredType) && 
                r.injectPoint.equals(injectPoint) && 
                (r.satisfaction == null ? satisfaction == null : r.satisfaction.equals(satisfaction)));
    }

    @Override
    public int hashCode() {
        return desiredType.hashCode() ^ injectPoint.hashCode() ^ (satisfaction == null ? 0 : satisfaction.hashCode());
    }

    @Override
    public String toString() {
        return "Desire(" + desiredType.getSimpleName() + ", " + injectPoint + ")";
    }

    private Object writeReplace() {
        return new SerialProxy(desiredType, injectPoint, satisfaction);
    }

    private void readObject(ObjectInputStream stream) throws ObjectStreamException {
        throw new InvalidObjectException("must use serialization proxy");
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final InjectionPoint injectionPoint;
        private final ClassProxy desiredType;
        private final Satisfaction satisfaction;

        public SerialProxy(Class<?> type, InjectionPoint ip, Satisfaction sat) {
            injectionPoint = ip;
            desiredType = ClassProxy.of(type);
            satisfaction = sat;
        }

        @SuppressWarnings("unchecked")
        private Object readResolve() throws ObjectStreamException {
            try {
                return new ReflectionDesire(desiredType.resolve(),
                                            injectionPoint,
                                            satisfaction);
            } catch (ClassNotFoundException e) {
                InvalidObjectException ex = new InvalidObjectException("cannot resolve " + desiredType);
                ex.initCause(e);
                throw ex;
            } catch (InvalidBindingException e) {
                InvalidObjectException ex = new InvalidObjectException("invalid binding");
                ex.initCause(e);
                throw ex;
            }
        }
    }
}
