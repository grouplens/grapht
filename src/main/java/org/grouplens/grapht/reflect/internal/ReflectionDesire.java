/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
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
import java.util.*;

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
                    for (int i = 0; i < ctor.getParameterTypes().length; i++) {
                        desires.add(new ReflectionDesire(new ConstructorParameterInjectionPoint(ctor, i)));
                    }
                } else {
                    // at the moment there can only be one injectable constructor
                    throw new InvalidBindingException(type, "More than one constructor with @Inject is not allowed");
                }
            }
        }
        
        // JSR 330 mandates that super class methods are injected first, so we
        // collect method injection points into a separate list and then reverse
        // it to get the ordering correct.
        List<Desire> groupDesires = Lists.newArrayList();
        
        // Must also keep track of methods overridden in the subtypes.
        Set<Signature> visitedMethods = new HashSet<Signature>();
        while(type != null) {
            for (Method m: type.getDeclaredMethods()) {
                Signature s = new Signature(m);
                if (!visitedMethods.contains(s) && m.getAnnotation(Inject.class) != null
                    && !Modifier.isStatic(m.getModifiers())) {
                    // have not seen this signature, and its an injection point
                    if (m.getParameterTypes().length > 0) {
                        for (int i = 0; i < m.getParameterTypes().length; i++) {
                            groupDesires.add(new ReflectionDesire(new SetterInjectionPoint(m, i)));
                        }
                    } else {
                        // hack to invoke no-argument injectable methods required by JSR 330
                        groupDesires.add(new ReflectionDesire(new NoArgumentInjectionPoint(m)));
                    }
                }
                // always add signature, because a subclass without @Inject
                // overrides any @Inject on the superclass's method declaration
                visitedMethods.add(s);
            }
            for (Field f: type.getDeclaredFields()) {
                if (f.getAnnotation(Inject.class) != null && !Modifier.isStatic(f.getModifiers())) {
                    // have not seen this field
                    groupDesires.add(new ReflectionDesire(new FieldInjectionPoint(f)));
                }
            }
            
            type = type.getSuperclass();
        }
        
        // after reversing this list, fields will be injected 
        // before methods as required
        Collections.reverse(groupDesires);
        desires.addAll(groupDesires);
        
        return Collections.unmodifiableList(desires);
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
    
    /*
     * Internal class to track a methods signature. Java's default reflection
     * doesn't give us a convenient way to record just this information.
     *
     * FIXME Document why we need this class more clearly
     */
    public static class Signature {
        private final String name;
        private final Type[] args;
        
        public Signature(Method m) {
            // FIXME Make it clearer what this code is supposed to do
            int mods = m.getModifiers();
            if (Modifier.isPublic(mods) || Modifier.isProtected(mods)) {
                // method overrides depends solely on method name
                name = m.getName();
            } else if (Modifier.isPrivate(mods)) {
                // method overrides depend on method name and class name
                name = m.getName() + m.getDeclaringClass().getCanonicalName();
            } else {
                // method overrides depend on method name and package,
                // since it is package-private
                Package pkg = m.getDeclaringClass().getPackage();
                if (pkg != null) {
                    name = m.getName() + pkg.getName();
                } else {
                    name = m.getName();
                }
            }
            args = m.getGenericParameterTypes();
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Signature)) {
                return false;
            }
            Signature s = (Signature) o;
            return s.name.equals(name) && Arrays.equals(args, s.args);
        }
        
        @Override
        public int hashCode() {
            return (name.hashCode() ^ Arrays.hashCode(args));
        }
    }
}
